package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class PaginatedHttpCheckDto extends PaginatedDto {
    private final List<HttpCheckDto> httpChecks;

    public PaginatedHttpCheckDto(List<HttpCheckDto> httpChecks,
                                   long totalElements,
                                   int totalPages,
                                   int number,
                                   int size) {
        super(totalElements, totalPages, number, size);
        this.httpChecks = httpChecks;
    }

    public List<HttpCheckDto> getHttpChecks() {
        return httpChecks;
    }
}
