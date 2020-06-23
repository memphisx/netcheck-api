package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class PaginatedDto<T> extends AbstractPaginatedDto {
    private final List<T> dtoList;

    public PaginatedDto(List<T> dtoList,
                        long totalElements,
                        int totalPages,
                        int number,
                        int size) {
        super(totalElements, totalPages, number, size);
        this.dtoList = dtoList;
    }

    public List<T> getDtoList() {
        return dtoList;
    }
}
