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

import com.bompotis.netcheck.api.model.DomainCheckModel;
import com.bompotis.netcheck.api.model.assembler.DomainCheckModelAssembler;
import com.bompotis.netcheck.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Created by Kyriakos Bompotis on 13/7/20.
 */
@RestController
@CrossOrigin(origins = {"${settings.cors.origin}"})
@RequestMapping(value = "/api/v1/check")
@Tag(name = "Domain Checks", description = "Operations for checking domains")
public class CheckController {

    private final DomainService domainService;

    @Autowired
    public CheckController(DomainService domainService) {
        this.domainService = domainService;
    }

    @Operation(summary = "Get current status of a domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current status of the domain",
                    content = { @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = DomainCheckModel.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid domain",
                    content = @Content)})
    @GetMapping(produces={"application/hal+json"}, path = "/{domain}")
    public ResponseEntity<DomainCheckModel> getDomainStatus(@PathVariable("domain") String domain) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        var status = domainService.check(domain);
        var domainCheckModel = new DomainCheckModelAssembler().toModel(status);
        domainCheckModel.add(linkTo(methodOn(DomainsController.class).getDomainStatus(domain)).withSelfRel());
        return ok(domainCheckModel);
    }
}
