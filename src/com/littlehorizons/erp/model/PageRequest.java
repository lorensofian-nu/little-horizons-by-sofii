package com.littlehorizons.erp.model;

/** Validated page request for database-backed list views. */
public record PageRequest(int pageNumber, int pageSize) {

    public PageRequest {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be positive.");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }
        if ((long) (pageNumber - 1) * pageSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page offset is too large.");
        }
    }

    public int offset() {
        return Math.multiplyExact(pageNumber - 1, pageSize);
    }
}
