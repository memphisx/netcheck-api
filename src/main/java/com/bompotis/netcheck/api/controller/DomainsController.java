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
import com.bompotis.netcheck.api.model.DomainCheckModel;
import com.bompotis.netcheck.api.model.DomainRequest;
import com.bompotis.netcheck.api.model.DomainResponse;
import com.bompotis.netcheck.api.model.HttpCheckModel;
import com.bompotis.netcheck.api.model.MetricModel;
import com.bompotis.netcheck.api.model.PatchOperation;
import com.bompotis.netcheck.api.model.StateModel;
import com.bompotis.netcheck.api.model.assembler.DomainCheckModelAssembler;
import com.bompotis.netcheck.api.model.assembler.DomainModelAssembler;
import com.bompotis.netcheck.api.model.assembler.DomainUpdateDtoAssembler;
import com.bompotis.netcheck.api.model.assembler.HttpCheckModelAssembler;
import com.bompotis.netcheck.api.model.assembler.MetricModelAssembler;
import com.bompotis.netcheck.api.model.assembler.StateModelAssembler;
import com.bompotis.netcheck.service.DomainService;
import com.bompotis.netcheck.service.MetricService;
import com.bompotis.netcheck.service.dto.DomainDto;
import com.bompotis.netcheck.service.dto.DomainsOptionsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;


/**
 * Created by Kyriakos Bompotis on 28/11/18.
 */
@RestController
@CrossOrigin(origins = {"${settings.cors.origin}"})
@RequestMapping(value = "/api/v1/domains")
@Tag(name = "Scheduled Domain Checks", description = "Operations for scheduled domain checks")
@Validated
public class DomainsController {

    private final DomainService domainService;

    private final MetricService metricService;

    @Autowired
    public DomainsController(DomainService domainService, MetricService metricService) {
        this.domainService = domainService;
        this.metricService = metricService;
    }

    @Operation(summary = "Get domains that are scheduled for periodical checking")
    @GetMapping(produces={"application/hal+json"})
    public ResponseEntity<CollectionModel<DomainResponse>> getDomains(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "showLastChecks", required = false) Boolean showLastChecks,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "desc", required = false) Boolean desc
    ) throws IOException, KeyManagementException, NoSuchAlgorithmException, EntityNotFoundException {
        var options = new DomainsOptionsDto.Builder()
                .page(page)
                .size(size)
                .showLastChecks(showLastChecks)
                .filter(filter)
                .sortBy(sortBy)
                .desc(desc)
                .build();
        return ok(new DomainModelAssembler().toCollectionModel(
                domainService.getPaginatedDomains(options),
                options)
        );
    }

    @Operation(summary = "Get scheduled domain config and its last checks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Config and last checks of the domain",
                    content = { @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = DomainResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Domain is not scheduled or no checks where found",
                    content = @Content)})
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}")
    public ResponseEntity<DomainResponse> getDomainStatus(
            @PathVariable("domain") String domain
    ) throws EntityNotFoundException {
        return domainService
                .getDomain(domain)
                .map(response -> ok(new DomainModelAssembler().toModel(response)))
                .orElseThrow(EntityNotFoundException::new);
    }

    @Operation(summary = "Schedule domain for periodic checks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was successfully added"),
            @ApiResponse(responseCode = "409", description = "Domain is already scheduled for checks")
    })
    @PutMapping(path = "/{domain}", consumes={"application/json"})
    public ResponseEntity<Object> addDomainToScheduler(@PathVariable("domain") String domain,
                                                       @Valid @RequestBody(required = false) DomainRequest domainRequest) {
        if (domainService.domainIsScheduled(domain)) {
            return status(HttpStatus.CONFLICT).build();
        }
        var domainDtoBuilder = new DomainDto.Builder().domain(domain);
        if (Optional.ofNullable(domainRequest).isPresent()) {
            domainDtoBuilder.checkFrequencyMinutes(domainRequest.checkFrequencyMinutes())
                            .endpoint(domainRequest.endpoint())
                            .httpPort(domainRequest.httpPort())
                            .httpsPort(domainRequest.httpsPort())
                            .timeoutMs(domainRequest.timeoutMs())
                            .withHeaders(domainRequest.headers());
        }
        domainService.scheduleDomainToCheck(domainDtoBuilder.build());
        return ok().build();
    }

    @Operation(summary = "Update configuration of a domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was successfully updated"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    @PatchMapping(path = "/{domain}")
    public ResponseEntity<Object> updateScheduledDomain(@PathVariable("domain") String domain,
                                                       @Valid @RequestBody List<PatchOperation> patchOperations
    ) throws EntityNotFoundException {
        domainService.updateDomainConfig(new DomainUpdateDtoAssembler(domain).toDto(patchOperations));
        return ok().build();
    }

    @Operation(summary = "Remove domain from schedule checks list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    @DeleteMapping(path = "/{domain}")
    public ResponseEntity<Object> deleteScheduledDomain(@PathVariable("domain") String domain)
            throws EntityNotFoundException {
        domainService.deleteScheduledDomain(domain);
        return ok().build();
    }

    @Operation(summary = "Get all previous checks for the given domain")
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}/history")
    public ResponseEntity<CollectionModel<DomainCheckModel>> getDomainsHistory(
            @PathVariable("domain") String domain,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new DomainCheckModelAssembler().toCollectionModel(
                    domainService.getDomainHistory(domain, page, size),
                    domain)
            );
        }
        return notFound().build();
    }

    @Operation(summary = "Get all previous HTTP checks for the given domain")
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}/http")
    public ResponseEntity<CollectionModel<HttpCheckModel>> getHttpChecks(
            @PathVariable("domain") String domain,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new HttpCheckModelAssembler().toCollectionModel(
                    domainService.getHttpDomainHistory(domain,page,size),
                    domain,
                    "http")
            );
        }
        return notFound().build();
    }

    @Operation(summary = "Get all previous HTTPS checks for the given domain")
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}/https")
    public ResponseEntity<CollectionModel<HttpCheckModel>> getHttpsChecks(
            @PathVariable("domain") String domain,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new HttpCheckModelAssembler().toCollectionModel(
                    domainService.getHttpsDomainHistory(domain,page,size),
                    domain,
                    "https")
            );
        }
        return notFound().build();
    }


    @Operation(summary = "Get all metrics for the given domain")
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}/metrics")
    public ResponseEntity<CollectionModel<MetricModel>> getDomainsMetrics(
            @PathVariable("domain") String domain,
            @RequestParam(name = "protocol") String protocol,
            @RequestParam(name = "period") String period,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new MetricModelAssembler().toCollectionModel(
                    metricService.getDomainMetrics(
                            domain,
                            period.toUpperCase(),
                            protocol.toUpperCase(),
                            page,
                            size
                    ),
                    domain,
                    protocol,
                    period
            ));
        }
        return notFound().build();
    }

    @Operation(summary = "Get all previous states for the given domain")
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}/states")
    public ResponseEntity<CollectionModel<StateModel>> getDomainsStates(
            @PathVariable("domain") String domain,
            @RequestParam(name = "protocol", required = false) String protocol,
            @RequestParam(name = "includeCertificates", required = false) Boolean includeCertificates,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            final var normalizedProtocol = Optional.ofNullable(protocol).orElse("HTTPS").toUpperCase();
            final var includeCertificateChanges = Optional.ofNullable(includeCertificates).orElse(false);
            return ok(new StateModelAssembler()
                    .toCollectionModel(
                            domainService.getDomainStates(
                                    domain,
                                    normalizedProtocol,
                                    includeCertificateChanges,
                                    page,
                                    size
                            ),
                            domain,
                            normalizedProtocol,
                            includeCertificateChanges
                    )
            );
        }
        return notFound().build();
    }

    @Operation(summary = "Get a previous domain check by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain check is found",
                    content = { @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = DomainCheckModel.class)) }),
            @ApiResponse(responseCode = "404", description = "Invalid id or domain is not scheduled",
                    content = @Content)})
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}/history/{id}")
    public ResponseEntity<DomainCheckModel> getDomainsHistoricEntry(
            @PathVariable("domain") String domain,
            @PathVariable("id") String id) {
        var result = new AtomicReference<ResponseEntity<DomainCheckModel>>();
        domainService.getDomainCheck(domain,id).ifPresentOrElse(
                domainCheck -> result.set(ok(new DomainCheckModelAssembler().toModel(domainCheck))),
                () -> result.set(notFound().build())
        );
        return result.get();
    }
}