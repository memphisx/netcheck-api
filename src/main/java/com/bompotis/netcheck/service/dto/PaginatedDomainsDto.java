package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 12/6/20.
 */
public class PaginatedDomainsDto extends PaginatedDto{
    private final List<String> domains;

    public PaginatedDomainsDto(List<String> domains,
                               long totalElements,
                               int totalPages,
                               int number,
                               int size) {
        super(totalElements, totalPages, number, size);
        this.domains = domains;
    }

    public List<String> getDomains() {
        return domains;
    }

}
