package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.CertificateModel;
import com.bompotis.netcheck.api.model.DomainCheckModel;
import com.bompotis.netcheck.api.model.HttpCheckModel;
import com.bompotis.netcheck.service.dto.DomainCheckDto;
import com.bompotis.netcheck.service.dto.PaginatedDomainCheckDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class DomainCheckModelAssembler extends PaginatedRepresentationModelAssemblerSupport<DomainCheckDto, DomainCheckModel> {

    public DomainCheckModelAssembler() {
        super(DomainsController.class, DomainCheckModel.class);
    }

    @Override
    public DomainCheckModel toModel(DomainCheckDto domainCheckDto) {
        HttpCheckModel httpCheck = null;
        HttpCheckModel httpsCheck = null;
        CertificateModel certificate = null;
        if (Optional.ofNullable(domainCheckDto.getHttpCheckDto()).isPresent()) {
            httpCheck = new HttpCheckModelAssembler().toModel(domainCheckDto.getHttpCheckDto());
            if (Optional.ofNullable(domainCheckDto.getHttpCheckDto().getId()).isPresent()) {
                httpCheck.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                        domainCheckDto.getDomain(),
                        domainCheckDto.getHttpCheckDto().getId())).withSelfRel()
                );
            }
        }
        if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto()).isPresent()) {
            httpsCheck = new HttpCheckModelAssembler().toModel(domainCheckDto.getHttpsCheckDto().getHttpCheckDto());
            if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getId()).isPresent()) {
                httpsCheck.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                        domainCheckDto.getDomain(),
                        domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getId())).withSelfRel()
                );
            }

            if (Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()).isPresent()) {
                certificate = new CertificateModelAssembler().toModel(domainCheckDto.getHttpsCheckDto().getIssuerCertificate());
            }
        }
        var domainCheckModel = new DomainCheckModel(httpCheck, httpsCheck, certificate);
        if (Optional.ofNullable(domainCheckDto.getId()).isPresent()) {
            domainCheckModel.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                    domainCheckDto.getDomain(),
                    domainCheckDto.getId())).withSelfRel()
            );
        }
        return domainCheckModel;
    }

    public CollectionModel<DomainCheckModel> toCollectionModel(PaginatedDomainCheckDto paginatedDomainCheckDto, String domain) {
        var historicEntries = new ArrayList<DomainCheckModel>();
        for (var domainCheckDto : paginatedDomainCheckDto.getDomainChecks()) {
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
