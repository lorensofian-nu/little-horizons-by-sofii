package com.littlehorizons.erp.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

/** Small bounded JDBC connection pool with close-to-return semantics. */
final class ConnectionPool {

    @FunctionalInterface
    interface ConnectionFactory {
        Connection open() throws SQLException;
    }

    private final ConnectionFactory factory;
    private final int maxSize;
    private final Object lock = new Object();
    private final Deque<Connection> idle = new ArrayDeque<>();
    private int total;

    ConnectionPool(ConnectionFactory factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    Connection borrow() throws SQLException {
        synchronized (lock) {
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
            while (true) {
                Connection physical = idle.pollFirst();
                if (physical != null) {
                    if (physical.isClosed()) {
                        total--;
                        continue;
                    }
                    return wrap(physical);
                }
                if (total < maxSize) {
                    physical = factory.open();
                    total++;
                    return wrap(physical);
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw new SQLException("Connection pool timeout.");
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

    private Connection wrap(Connection physical) {
        InvocationHandler handler = new PooledConnectionHandler(this, physical);
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, handler);
    }

    private void release(Connection physical) {
        synchronized (lock) {
            try {
                if (!physical.getAutoCommit()) {
                    physical.rollback();
                }
                physical.setReadOnly(false);
                physical.setAutoCommit(true);
                physical.clearWarnings();
                idle.addLast(physical);
            } catch (SQLException exception) {
                closePhysical(physical);
                total--;
            }
            lock.notifyAll();
        }
    }

    void closeAll() {
        synchronized (lock) {
            while (!idle.isEmpty()) {
                closePhysical(idle.removeFirst());
                total--;
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
                    pool.release(physical);
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
