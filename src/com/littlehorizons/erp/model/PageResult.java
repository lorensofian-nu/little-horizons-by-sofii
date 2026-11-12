package com.littlehorizons.erp.model;

import java.util.List;

/** One bounded page returned by a DAO. */
public record PageResult<T>(List<T> items, PageRequest request, boolean hasNext) {
}
