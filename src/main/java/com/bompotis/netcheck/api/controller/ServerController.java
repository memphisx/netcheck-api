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
package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.exception.EntityNotFoundException;
import com.bompotis.netcheck.api.exception.InvalidAuthHeaders;
import com.bompotis.netcheck.api.exception.UnauthorizedStatusException;
import com.bompotis.netcheck.api.model.PatchOperation;
import com.bompotis.netcheck.api.model.RegisterServerRequest;
import com.bompotis.netcheck.api.model.ServerDefinitionModel;
import com.bompotis.netcheck.api.model.ServerMetricModel;
import com.bompotis.netcheck.api.model.ServerMetricRequest;
import com.bompotis.netcheck.api.model.ServerModel;
import com.bompotis.netcheck.api.model.assembler.ServerDefinitionAssembler;
import com.bompotis.netcheck.api.model.assembler.ServerMetricModelAssembler;
import com.bompotis.netcheck.api.model.assembler.ServerModelAssembler;
import com.bompotis.netcheck.api.model.assembler.ServerUpdateDtoAssembler;
import com.bompotis.netcheck.service.ServerMetricsService;
import com.bompotis.netcheck.service.dto.RequestOptionsDto;
import com.bompotis.netcheck.service.dto.ServerDto;
import com.bompotis.netcheck.service.dto.ServerMetricDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Created by Kyriakos Bompotis on 25/8/20.
 */
@RestController
@CrossOrigin(origins = {"${settings.cors.origin}"})
@RequestMapping(value = "/api/v1/server")
@Tag(name = "Server Metrics", description = "Operations for server sent metrics")
public class ServerController {

    private final ServerMetricsService serverMetricsService;

    @Autowired
    public ServerController(ServerMetricsService serverMetricsService) {
        this.serverMetricsService = serverMetricsService;
    }

    @PostMapping
    public ResponseEntity<ServerDto> generateNewServerConfig(
            @Valid @RequestBody RegisterServerRequest registerServerRequest
    ) {
        return ok(serverMetricsService.registerServer(
                registerServerRequest.getServerName(),
                registerServerRequest.getDescription()
        ));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<ServerModel>> getServers(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "desc", required = false) Boolean desc) throws EntityNotFoundException {
        var requestOptions = new RequestOptionsDto.Builder()
                .desc(desc)
                .size(size)
                .page(page)
                .filter(filter)
                .sortBy(sortBy)
                .build();
        return ok(new ServerModelAssembler()
                .toCollectionModel(serverMetricsService.getServers(requestOptions),requestOptions));
    }

    @GetMapping("/{serverId}")
    public ResponseEntity<ServerModel> getServerConfig(
            @PathVariable("serverId") String serverId
    ) throws EntityNotFoundException {
        return ok(new ServerModelAssembler()
                .toModelWithSelfLink(serverMetricsService.getServer(serverId)));
    }

    @PostMapping("/{serverId}/metric")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server metric successfully added"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<String> postServerMetric(
            @PathVariable("serverId") String serverId,
            @Valid @RequestBody ServerMetricRequest serverMetricRequest,
            @RequestHeader("Authorization") String authHeader) throws InvalidAuthHeaders, UnauthorizedStatusException {
        serverMetricsService.addServerMetrics(new ServerMetricDto.Builder(serverId)
                .metrics(serverMetricRequest.getMetrics())
                .collectedAt(serverMetricRequest.getCollectedAt())
                .authHeader(authHeader)
                .build()

        );

        return ok().build();
    }

    @PostMapping("/{serverId}/definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server metrics successfully added or updated"),
            @ApiResponse(responseCode = "404", description = "Server not found")
    })
    public ResponseEntity<String> postServerMetricDefinitions(
            @PathVariable("serverId") String serverId,
            @Valid @RequestBody List<ServerDefinitionModel> serverDefinitionModels) throws EntityNotFoundException {
        serverMetricsService.addServerMetricDefinitions(
                serverId,
                serverDefinitionModels
                        .stream()
                        .map(serverDefinitionModel -> new ServerDefinitionAssembler().toDto(serverDefinitionModel))
                        .collect(Collectors.toCollection(ArrayList::new))
                );
        return ok().build();
    }

    @PutMapping("/{serverId}/definition/{fieldName}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server metric successfully added or updated"),
            @ApiResponse(responseCode = "404", description = "Server not found")
    })
    public ResponseEntity<String> postServerMetricDefinitions(
            @PathVariable("serverId") String serverId,
            @PathVariable("fieldName") String fieldName,
            @Valid @RequestBody ServerDefinitionModel serverDefinitionModel) throws EntityNotFoundException {
        serverMetricsService.upsertServerMetricDefinition(
                serverId,
                new ServerDefinitionAssembler().toDto(serverDefinitionModel)
        );
        return ok().build();
    }

    @Operation(summary = "Get Server Metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server metrics"),
            @ApiResponse(responseCode = "404", description = "Server not found")
    })
    @GetMapping("/{serverId}/metric")
    public ResponseEntity<CollectionModel<ServerMetricModel>> getServerMetrics(
            @PathVariable("serverId") String serverId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "desc", required = false) Boolean desc) {
        var requestOptions = new RequestOptionsDto.Builder()
                .desc(desc)
                .size(size)
                .page(page)
                .sortBy(sortBy)
                .build();
        serverMetricsService.getServerMetrics(serverId,requestOptions);
        return ok(new ServerMetricModelAssembler()
                .toCollectionModel(
                        serverMetricsService.getServerMetrics(serverId,requestOptions),
                        requestOptions,serverId
                )
        );
    }

    @Operation(summary = "Remove a server and all its metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server was successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Server not found")
    })
    @DeleteMapping(path = "/{serverId}")
    public ResponseEntity<Object> deleteServer(
            @PathVariable("serverId") String serverId) throws EntityNotFoundException {
        serverMetricsService.deleteServer(serverId);
        return ok().build();
    }

    @Operation(summary = "Update configuration of a server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server was successfully updated"),
            @ApiResponse(responseCode = "404", description = "server not found")
    })
    @PatchMapping(path = "/{serverId}")
    public ResponseEntity<Object> updateServerConfig(
            @PathVariable("serverId") String serverId,
            @Valid @RequestBody List<PatchOperation> patchOperations) throws EntityNotFoundException {
        serverMetricsService.updateServerConfig(new ServerUpdateDtoAssembler(serverId).toDto(patchOperations));
        return ok().build();
    }
}
