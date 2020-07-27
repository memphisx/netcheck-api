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
import com.bompotis.netcheck.api.model.StateModel;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import com.bompotis.netcheck.service.dto.StateDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 24/6/20.
 */
public class StateModelAssembler extends PaginatedRepresentationModelAssemblerSupport<StateDto, StateModel> {
    public StateModelAssembler() {
        super(DomainsController.class, StateModel.class);
    }

    @Override
    public StateModel toModel(StateDto stateDto) {
        return new StateModel(
                stateDto.getHostname(),
                stateDto.getStatusCode(),
                stateDto.getTimeCheckedOn(),
                stateDto.getReason(),
                stateDto.getDnsResolves(),
                stateDto.getProtocol(),
                stateDto.getRedirectUri(),
                stateDto.isUp(),
                stateDto.getDuration().getSeconds());
    }

    public CollectionModel<StateModel> toCollectionModel(PaginatedDto<StateDto> paginatedStatesDto, String domain, String protocol, Boolean includeCertificates) {
        final var stateModels = this.toCollectionModel(paginatedStatesDto.getDtoList()).getContent();
        final var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomainsStates(domain, protocol, includeCertificates, paginatedStatesDto.getNumber(), paginatedStatesDto.getSize()))
                .withSelfRel()
        );
        if (isValidPage(paginatedStatesDto.getNumber(),paginatedStatesDto.getTotalPages())) {
            if (isNotLastPage(paginatedStatesDto.getNumber(), paginatedStatesDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsStates(domain, protocol, includeCertificates, paginatedStatesDto.getNumber()+1, paginatedStatesDto.getSize()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedStatesDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsStates(domain, protocol, includeCertificates,paginatedStatesDto.getNumber()-1, paginatedStatesDto.getSize()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                stateModels,
                new PagedModel.PageMetadata(
                        stateModels.size(),
                        paginatedStatesDto.getNumber(),
                        paginatedStatesDto.getTotalElements(),
                        paginatedStatesDto.getTotalPages()
                ),
                links
        );
    }
}
