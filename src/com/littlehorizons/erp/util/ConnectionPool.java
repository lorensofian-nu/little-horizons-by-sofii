package com.littlehorizons.erp.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/** Small bounded JDBC connection pool with close-to-return semantics. */
final class ConnectionPool {

    @FunctionalInterface
    interface ConnectionFactory {
        Connection open() throws SQLException;
    }

    /** Maximum wait for a pooled connection before failing with a diagnostic. */
    private static final long BORROW_TIMEOUT_SECONDS = 30;
    /** Seconds passed to {@link Connection#isValid} when reusing idle connections. */
    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final ConnectionFactory factory;
    private final int maxSize;
    private final Object lock = new Object();
    private final Deque<Connection> idle = new ArrayDeque<>();
    private final Map<PooledConnectionHandler, String> outstanding = new LinkedHashMap<>();
    private int total;

    ConnectionPool(ConnectionFactory factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    Connection borrow() throws SQLException {
        synchronized (lock) {
            long deadline = System.nanoTime()
                    + java.util.concurrent.TimeUnit.SECONDS.toNanos(BORROW_TIMEOUT_SECONDS);
            while (true) {
                Connection physical = idle.pollFirst();
                if (physical != null) {
                    total--;
                    if (!isReusable(physical)) {
                        continue;
                    }
                    total++;
                    PooledConnectionHandler handler = new PooledConnectionHandler(this, physical);
                    outstanding.put(handler, allocationSite());
                    return (Connection) Proxy.newProxyInstance(
                            Connection.class.getClassLoader(),
                            new Class<?>[]{Connection.class}, handler);
                }
                if (total < maxSize) {
                    physical = factory.open();
                    total++;
                    PooledConnectionHandler handler = new PooledConnectionHandler(this, physical);
                    outstanding.put(handler, allocationSite());
                    return (Connection) Proxy.newProxyInstance(
                            Connection.class.getClassLoader(),
                            new Class<?>[]{Connection.class}, handler);
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw leakTimeoutException();
                }
                try {
                    java.util.concurrent.TimeUnit.NANOSECONDS.timedWait(lock, remaining);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for a database connection.",
                            exception);
                }
            }
        }
    }

    /**
     * Checks that an idle connection is still usable before lending it; a
     * zombie connection (network drop, server timeout) is closed and dropped
     * from the pool instead of being handed out.
     */
    private boolean isReusable(Connection physical) throws SQLException {
        boolean valid = false;
        try {
            valid = !physical.isClosed() && physical.isValid(VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException exception) {
            LoggerUtil.logError("ConnectionPool.validateConnection", exception);
        }
        if (!valid) {
            closePhysical(physical);
            return false;
        }
        return true;
    }

    /** Builds the timeout error with leak diagnostics for open connections. */
    private SQLException leakTimeoutException() {
        int leaked = outstanding.size();
        StringBuilder message = new StringBuilder()
                .append("Connection pool timeout after ")
                .append(BORROW_TIMEOUT_SECONDS)
                .append(" seconds; ")
                .append(leaked)
                .append(" connection(s) are still outstanding, which suggests a leak "
                        + "(a borrower that never closed its connection).");
        SQLException exception = new SQLException(message.toString());
        for (String site : outstanding.values()) {
            exception.addSuppressed(new SQLException("Connection borrowed at: " + site));
        }
        LoggerUtil.logError("ConnectionPool.borrow", exception);
        return exception;
    }

    /** Captures a compact allocation stack trace for leak diagnostics. */
    private String allocationSite() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder site = new StringBuilder();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.equals(getClass().getName())
                    || className.startsWith("java.lang.Thread")
                    || className.startsWith("jdk.internal.")) {
                continue;
            }
            site.append(element).append(" <- ");
            if (site.length() > 300) {
                break;
            }
        }
        return site.length() > 0 ? site.substring(0, site.length() - 4) : "<unknown>";
    }

    private void release(PooledConnectionHandler handler) {
        Connection physical = handler.physical;
        synchronized (lock) {
            outstanding.remove(handler);
            try {
                if (!physical.getAutoCommit()) {
                    physical.rollback();
                }
                physical.setReadOnly(false);
                physical.setAutoCommit(true);
                physical.clearWarnings();
                idle.addLast(physical);
                total++;
            } catch (SQLException exception) {
                closePhysical(physical);
            }
            lock.notifyAll();
        }
    }

    void closeAll() {
        synchronized (lock) {
            while (!idle.isEmpty()) {
                closePhysical(idle.removeFirst());
            }
            if (!outstanding.isEmpty()) {
                LoggerUtil.logError("ConnectionPool.closeAll",
                        new SQLException(outstanding.size()
                                + " connection(s) were still outstanding at shutdown."));
            }
            lock.notifyAll();
        }
    }

    private void closePhysical(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Best effort during pool cleanup.
        }
    }

    private static final class PooledConnectionHandler implements InvocationHandler {
        private final ConnectionPool pool;
        private final Connection physical;
        private boolean returned;

        private PooledConnectionHandler(ConnectionPool pool, Connection physical) {
            this.pool = pool;
            this.physical = physical;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("close") && method.getParameterCount() == 0) {
                if (!returned) {
                    returned = true;
                    pool.release(this);
                }
                return null;
            }
            if (method.getName().equals("isClosed") && method.getParameterCount() == 0) {
                return returned || physical.isClosed();
            }
            if (returned) {
                throw new SQLException("Connection is already closed.");
            }
            try {
                return method.invoke(physical, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }
    }
}
