package com.bompotis.netcheck.service.Dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 12/6/20.
 */
public class PaginatedDomainCheckDto extends PaginatedDto {

    private final List<DomainCheckDto> domainChecks;

    public PaginatedDomainCheckDto(List<DomainCheckDto> domainChecks,
                                   long totalElements,
                                   int totalPages,
                                   int number,
                                   int size) {
        super(totalElements, totalPages, number, size);
        this.domainChecks = domainChecks;
    }

    public List<DomainCheckDto> getDomainChecks() {
        return domainChecks;
    }
}
