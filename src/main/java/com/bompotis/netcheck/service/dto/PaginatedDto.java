/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
