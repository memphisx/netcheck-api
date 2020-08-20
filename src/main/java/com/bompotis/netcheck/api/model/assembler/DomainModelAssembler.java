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
import com.bompotis.netcheck.api.model.DomainModel;
import com.bompotis.netcheck.service.dto.DomainDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class DomainModelAssembler extends PaginatedRepresentationModelAssemblerSupport<DomainDto, DomainModel> {
    public DomainModelAssembler() {
        super(DomainsController.class, DomainModel.class);
    }

    @Override
    public DomainModel toModel(DomainDto domainDto) {
        var lastDomainChecks = Optional.ofNullable(domainDto.getLastDomainCheck()).isPresent() ?
                new DomainCheckModelAssembler().toModel(domainDto.getLastDomainCheck()) : null;
        return new DomainModel(
                domainDto.getDomain(),
                lastDomainChecks,
                domainDto.getCreatedAt(),
                domainDto.getCheckFrequencyMinutes()
        );
    }

    public CollectionModel<DomainModel> toCollectionModel(PaginatedDto<DomainDto> paginatedDomainsDto, Boolean compact) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        var domainModels = new ArrayList<DomainModel>();
        for (var domain : paginatedDomainsDto.getDtoList()) {
            var domainModel = this.toModel(domain);
            domainModel.add(
                    linkTo(methodOn(DomainsController.class).getDomainStatus(domainModel.getDomain())).withSelfRel()
            );
            domainModels.add(domainModel);
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomains(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getSize(), compact))
                .withSelfRel()
        );
        if (isValidPage(paginatedDomainsDto.getNumber(),paginatedDomainsDto.getTotalPages())) {
            if (isNotLastPage(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(paginatedDomainsDto.getNumber()+1, paginatedDomainsDto.getSize(), compact))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedDomainsDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(paginatedDomainsDto.getNumber()-1, paginatedDomainsDto.getSize(), compact))
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
