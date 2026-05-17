package com.reything.subsmentbe.dto.common;

public record PaginationInfo(
        int page,
        int limit,
        long total,
        int pages
) {
}
