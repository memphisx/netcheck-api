package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class PaginatedSingleDto<T> extends PaginatedDto{
    private final List<T> dtoList;

    public PaginatedSingleDto(List<T> dtoList,
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
