package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 12/6/20.
 */
public class PaginatedDomainCheckDto extends PaginatedDto {

    private final List<DomainStatusDto> domainChecks;

    public PaginatedDomainCheckDto(List<DomainStatusDto> domainChecks,
                                   long totalElements,
                                   int totalPages,
                                   int number,
                                   int size) {
        super(totalElements, totalPages, number, size);
        this.domainChecks = domainChecks;
    }

    public List<DomainStatusDto> getDomainChecks() {
        return domainChecks;
    }
}
