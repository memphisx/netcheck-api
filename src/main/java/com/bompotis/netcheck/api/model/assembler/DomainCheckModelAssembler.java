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
import com.bompotis.netcheck.api.model.CertificateModel;
import com.bompotis.netcheck.api.model.DomainCheckModel;
import com.bompotis.netcheck.api.model.HttpCheckModel;
import com.bompotis.netcheck.service.dto.DomainCheckDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class DomainCheckModelAssembler extends PaginatedRepresentationModelAssemblerSupport<DomainCheckDto, DomainCheckModel> {

    public DomainCheckModelAssembler() {
        super(DomainsController.class, DomainCheckModel.class);
    }

    @NonNull
    @Override
    public DomainCheckModel toModel(DomainCheckDto domainCheckDto) {
        var httpChecks = new ArrayList<HttpCheckModel>();
        CertificateModel issuerCertificate = null;
        List<CertificateModel> caCertificates = null;
        if (Optional.ofNullable(domainCheckDto.getHttpCheckDto()).isPresent()) {
            var httpCheck = new HttpCheckModelAssembler().toModel(domainCheckDto.getHttpCheckDto());
            if (Optional.ofNullable(domainCheckDto.getHttpCheckDto().getId()).isPresent()) {
                httpCheck.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                        domainCheckDto.getDomain(),
                        domainCheckDto.getHttpCheckDto().getId())).withSelfRel()
                );
            }
            httpChecks.add(httpCheck);
        }
        if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto()).isPresent()) {
            var httpsCheck = new HttpCheckModelAssembler().toModel(domainCheckDto.getHttpsCheckDto().getHttpCheckDto());
            if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getId()).isPresent()) {
                httpsCheck.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                        domainCheckDto.getDomain(),
                        domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getId())).withSelfRel()
                );
            }
            httpChecks.add(httpsCheck);
            if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()).isPresent()) {
                issuerCertificate = new CertificateModelAssembler().toModel(domainCheckDto.getHttpsCheckDto().getIssuerCertificate());
            }
            if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getCaCertificates()).isPresent()) {
                caCertificates = domainCheckDto
                        .getHttpsCheckDto()
                        .getCaCertificates()
                        .stream()
                        .map(cert -> new CertificateModelAssembler().toModel(cert))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }
        var domainCheckModel = new DomainCheckModel(
                domainCheckDto.getDomain(),
                domainCheckDto.getMonitored(),
                httpChecks,
                issuerCertificate,
                caCertificates
        );

        if (Optional.ofNullable(domainCheckDto.getId()).isPresent()) {
            domainCheckModel.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                    domainCheckDto.getDomain(),
                    domainCheckDto.getId())).withSelfRel()
            );
        }
        return domainCheckModel;
    }

    public CollectionModel<DomainCheckModel> toCollectionModel(PaginatedDto<DomainCheckDto> paginatedDomainCheckDto, String domain) {
        var historicEntries = new ArrayList<DomainCheckModel>();
        for (var domainCheckDto : paginatedDomainCheckDto.getDtoList()) {
            historicEntries.add(new DomainCheckModelAssembler().toModel(domainCheckDto));
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomainsHistory(domain, paginatedDomainCheckDto.getNumber(), paginatedDomainCheckDto.getSize())
        ).withSelfRel());
        if (isValidPage(paginatedDomainCheckDto.getNumber(),paginatedDomainCheckDto.getTotalPages())) {
            if (isNotLastPage(paginatedDomainCheckDto.getNumber(), paginatedDomainCheckDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsHistory(domain,paginatedDomainCheckDto.getNumber()+1, paginatedDomainCheckDto.getSize()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedDomainCheckDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsHistory(domain,paginatedDomainCheckDto.getNumber()-1, paginatedDomainCheckDto.getSize()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                historicEntries,
                new PagedModel.PageMetadata(
                        historicEntries.size(),
                        paginatedDomainCheckDto.getNumber(),
                        paginatedDomainCheckDto.getTotalElements(),
                        paginatedDomainCheckDto.getTotalPages()),
                links
        );
    }
}
