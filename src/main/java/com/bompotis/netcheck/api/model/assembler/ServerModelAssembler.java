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

import com.bompotis.netcheck.api.controller.ServerController;
import com.bompotis.netcheck.api.exception.EntityNotFoundException;
import com.bompotis.netcheck.api.model.ServerModel;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import com.bompotis.netcheck.service.dto.RequestOptionsDto;
import com.bompotis.netcheck.service.dto.ServerDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 4/9/20.
 */
public class ServerModelAssembler extends PaginatedRepresentationModelAssemblerSupport<ServerDto, ServerModel> {

    public ServerModelAssembler() {
        super(ServerController.class, ServerModel.class);
    }

    @NonNull
    @Override
    public ServerModel toModel(ServerDto dto) {
        return new ServerModel(
                dto.getServerId(),
                dto.getServerName(),
                dto.getDescription(),
                dto.getPassword(),
                dto.getDateAdded(),
                dto.getServerDefinitionDtos()
                        .stream()
                        .map(serverDefinitionDto -> new ServerDefinitionAssembler().toModel(serverDefinitionDto))
                        .collect(Collectors.toCollection(ArrayList::new))

        );
    }

    public ServerModel toModelWithSelfLink(ServerDto dto) throws EntityNotFoundException {
        return toModel(dto)
                .add(linkTo(methodOn(ServerController.class).getServerConfig(dto.getServerId())).withSelfRel());
    }

    public CollectionModel<ServerModel> toCollectionModel(
            PaginatedDto<ServerDto> paginateServerDtos,
            RequestOptionsDto requestOptionsDto
    ) throws EntityNotFoundException {
        var servers = new ArrayList<ServerModel>();
        for (var entity: paginateServerDtos.getDtoList()) {
            servers.add(toModelWithSelfLink(entity));
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(ServerController.class)
                .getServers(
                        paginateServerDtos.getNumber(),
                        paginateServerDtos.getSize(),
                        requestOptionsDto.getFilter(),
                        requestOptionsDto.getSortBy(),
                        requestOptionsDto.getDesc()))
                .withSelfRel());
        if (isValidPage(paginateServerDtos.getNumber(),paginateServerDtos.getTotalPages())) {
            if (isNotLastPage(paginateServerDtos.getNumber(), paginateServerDtos.getTotalPages())) {
                links.add(linkTo(methodOn(ServerController.class)
                        .getServers(
                                paginateServerDtos.getNumber()+1,
                                paginateServerDtos.getSize(),
                                requestOptionsDto.getFilter(),
                                requestOptionsDto.getSortBy(),
                                requestOptionsDto.getDesc()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotLastPage(paginateServerDtos.getNumber(), paginateServerDtos.getTotalPages())) {
                links.add(linkTo(methodOn(ServerController.class)
                        .getServers(
                                paginateServerDtos.getNumber()-1,
                                paginateServerDtos.getSize(),
                                requestOptionsDto.getFilter(),
                                requestOptionsDto.getSortBy(),
                                requestOptionsDto.getDesc()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                servers,
                new PagedModel.PageMetadata(
                        servers.size(),
                        paginateServerDtos.getNumber(),
                        paginateServerDtos.getTotalElements(),
                        paginateServerDtos.getTotalPages()),
                links
        );
    }

}
