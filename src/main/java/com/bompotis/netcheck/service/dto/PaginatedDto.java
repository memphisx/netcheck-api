package com.bompotis.netcheck.service.dto;

/**
 * Created by Kyriakos Bompotis on 12/6/20.
 */
public abstract class PaginatedDto {

    private final long totalElements;
    private final int totalPages;
    private final int number;
    private final int size;

    public PaginatedDto(long totalElements,
                        int totalPages,
                        int number,
                        int size) {
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }
}
