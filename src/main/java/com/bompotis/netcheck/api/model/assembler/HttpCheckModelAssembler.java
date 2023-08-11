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
import com.bompotis.netcheck.api.model.HttpCheckModel;
import com.bompotis.netcheck.service.dto.HttpCheckDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class HttpCheckModelAssembler extends PaginatedRepresentationModelAssemblerSupport<HttpCheckDto, HttpCheckModel> {

    public HttpCheckModelAssembler() {
        super(DomainsController.class, HttpCheckModel.class);
    }

    @NonNull
    @Override
    public HttpCheckModel toModel(HttpCheckDto httpCheckDto) {
        return new HttpCheckModel(
                httpCheckDto.getHostname(),
                httpCheckDto.getStatusCode(),
                httpCheckDto.getTimeCheckedOn(),
                httpCheckDto.getResponseTimeNs(),
                httpCheckDto.getDnsResolved(),
                httpCheckDto.getIpAddress(),
                httpCheckDto.getProtocol(),
                httpCheckDto.getRedirectUri(),
                httpCheckDto.isUp(),
                httpCheckDto.getErrorMessage());
    }

    public CollectionModel<HttpCheckModel> toCollectionModel(PaginatedDto<HttpCheckDto> paginatedHttpCheckDto, String domain, String protocol) {
        var httpModels = paginatedHttpCheckDto.getDtoList().stream().map(this::toModel).collect(Collectors.toCollection(ArrayList::new));
        ResponseEntity<CollectionModel<HttpCheckModel>> method;
        var links = new ArrayList<Link>();
        if (isValidPage(paginatedHttpCheckDto.getNumber(),paginatedHttpCheckDto.getTotalPages())) {
            if (protocol.equals("http")) {
                method = methodOn(DomainsController.class)
                        .getHttpChecks(domain,paginatedHttpCheckDto.getNumber()+1, paginatedHttpCheckDto.getSize());
                links.add(linkTo(methodOn(DomainsController.class)
                        .getHttpChecks(domain, paginatedHttpCheckDto.getNumber(), paginatedHttpCheckDto.getSize()))
                        .withSelfRel()
                );
            }
            else {
                method = methodOn(DomainsController.class)
                        .getHttpsChecks(domain,paginatedHttpCheckDto.getNumber()+1, paginatedHttpCheckDto.getSize());
                links.add(linkTo(methodOn(DomainsController.class)
                        .getHttpsChecks(domain, paginatedHttpCheckDto.getNumber(), paginatedHttpCheckDto.getSize()))
                        .withSelfRel()
                );
            }
            if (isNotLastPage(paginatedHttpCheckDto.getNumber(), paginatedHttpCheckDto.getTotalPages())) {
                links.add(linkTo(method).withRel(IanaLinkRelations.NEXT));
            }
            if (isNotFirstPage(paginatedHttpCheckDto.getNumber())) {
                links.add(linkTo(method).withRel(IanaLinkRelations.PREVIOUS));
            }
        }
        return PagedModel.of(
                httpModels,
                new PagedModel.PageMetadata(
                        httpModels.size(),
                        paginatedHttpCheckDto.getNumber(),
                        paginatedHttpCheckDto.getTotalElements(),
                        paginatedHttpCheckDto.getTotalPages()
                ),
                links
        );
    }

}
