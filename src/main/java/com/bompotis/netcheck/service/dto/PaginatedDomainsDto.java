package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 12/6/20.
 */
public class PaginatedDomainsDto extends PaginatedDto{
    private final List<DomainDto> domains;

    public PaginatedDomainsDto(List<DomainDto> domains,
                               long totalElements,
                               int totalPages,
                               int number,
                               int size) {
        super(totalElements, totalPages, number, size);
        this.domains = domains;
    }

    public List<DomainDto> getDomains() {
        return domains;
    }

}
