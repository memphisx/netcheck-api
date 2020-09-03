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
package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.DomainResponse;
import com.bompotis.netcheck.service.dto.DomainDto;
import com.bompotis.netcheck.service.dto.DomainsOptionsDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class DomainModelAssembler extends PaginatedRepresentationModelAssemblerSupport<DomainDto, DomainResponse> {
    public DomainModelAssembler() {
        super(DomainsController.class, DomainResponse.class);
    }

    @Override
    public DomainResponse toModel(DomainDto domainDto) {
        var lastDomainChecks = Optional.ofNullable(domainDto.getLastDomainCheck()).isPresent() ?
                new DomainCheckModelAssembler().toModel(domainDto.getLastDomainCheck()) : null;
        var obfuscatedHeaders = new HashMap<String, String>();
        Optional.ofNullable(domainDto.getHeaders()).orElse(new HashMap<>()).keySet().forEach(key -> {
            if (key.toLowerCase().contains("authorization") || key.toLowerCase().contains("authenticate")) {
                obfuscatedHeaders.put(key, "*****");
            } else {
                obfuscatedHeaders.put(key, domainDto.getHeaders().get(key));
            }
        });
        return new DomainResponse(
                domainDto.getDomain(),
                lastDomainChecks,
                domainDto.getCreatedAt(),
                domainDto.getCheckFrequencyMinutes(),
                domainDto.getEndpoint(),
                obfuscatedHeaders,
                domainDto.getTimeoutMs()
        );
    }

    public CollectionModel<DomainResponse> toCollectionModel(
            PaginatedDto<DomainDto> paginatedDomainsDto,
            DomainsOptionsDto options) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        var domainModels = new ArrayList<DomainResponse>();
        for (var domain : paginatedDomainsDto.getDtoList()) {
            var domainModel = this.toModel(domain);
            domainModel.add(
                    linkTo(methodOn(DomainsController.class).getDomainStatus(domainModel.getDomain())).withSelfRel()
            );
            domainModels.add(domainModel);
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomains(
                        paginatedDomainsDto.getNumber(),
                        paginatedDomainsDto.getSize(),
                        options.getShowLastChecks(),
                        options.getFilter(),
                        options.getSortBy(),
                        options.getDesc()))
                .withSelfRel()
        );
        if (isValidPage(paginatedDomainsDto.getNumber(),paginatedDomainsDto.getTotalPages())) {
            if (isNotLastPage(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(
                                paginatedDomainsDto.getNumber()+1,
                                paginatedDomainsDto.getSize(),
                                options.getShowLastChecks(),
                                options.getFilter(),
                                options.getSortBy(),
                                options.getDesc()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedDomainsDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(
                                paginatedDomainsDto.getNumber()-1,
                                paginatedDomainsDto.getSize(),
                                options.getShowLastChecks(),
                                options.getFilter(),
                                options.getSortBy(),
                                options.getDesc()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                domainModels,
                new PagedModel.PageMetadata(
                        domainModels.size(),
                        paginatedDomainsDto.getNumber(),
                        paginatedDomainsDto.getTotalElements(),
                        paginatedDomainsDto.getTotalPages()
                ),
                links
        );
    }
}
