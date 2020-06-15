package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.models.CertificateModel;
import com.bompotis.netcheck.api.models.DomainCheckModel;
import com.bompotis.netcheck.api.models.DomainModel;
import com.bompotis.netcheck.api.models.DomainStatusModel;
import com.bompotis.netcheck.service.DomainService;
import com.bompotis.netcheck.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Created by Kyriakos Bompotis on 28/11/18.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/domains")
public class DomainsController {

    private final DomainService domainService;

    @Autowired
    public DomainsController(DomainService domainService) {
        this.domainService = domainService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<CollectionModel<DomainModel>> getDomains(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) throws IOException {
        return ok(convertToPagedDomainModels(domainService.getPaginatedDomains(page,size)));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}")
    public ResponseEntity<DomainStatusModel> getDomainStatus(
            @PathVariable("domain") String domain,
            @RequestParam(name = "store", required = false) Boolean store) throws IOException {
        var status = domainService.check(domain);
        if(Optional.ofNullable(store).isPresent() && store) {
            domainService.storeResult(status);
        }
        var domainStatusModel = convertToDomainStatusModel(status);
        domainStatusModel.add(linkTo(methodOn(DomainsController.class).getDomainStatus(domain,store)).withSelfRel());
        return ok(domainStatusModel);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{domain}")
    public ResponseEntity<Object> addDomainToScheduler(@PathVariable("domain") String domain) {
        domainService.scheduleDomainToCheck(domain);
        return ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/history")
    public ResponseEntity<CollectionModel<DomainCheckModel>> getDomainsHistory(@PathVariable("domain") String domain,
                                                                               @RequestParam(name = "page", required = false) Integer page,
                                                                               @RequestParam(name = "size", required = false) Integer size) {
        return ok(convertToPagedDomainHistoricEntryModel(domainService.getDomainHistory(domain,page,size), domain));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/history/{id}")
    public ResponseEntity<DomainCheckModel> getDomainsHistoricEntry(@PathVariable("domain") String domain, @PathVariable("id") String id) {
        var optionalEntity = domainService.getDomainCheck(domain,id);
        if (optionalEntity.isEmpty()) {
            return notFound().build();
        }
        return ok(convertToDomainHistoricEntry(optionalEntity.get()));
    }

    private CollectionModel<DomainModel> convertToPagedDomainModels(PaginatedDomainsDto paginatedDomainsDto) throws IOException {
        var domainModels = new ArrayList<DomainModel>();
        for (String domain : paginatedDomainsDto.getDomains()) {
            var domainModel = new DomainModel(domain);
            domainModel.add(
                    linkTo(methodOn(DomainsController.class).getDomainStatus(domainModel.getDomain(), false)).withSelfRel()
            );
            domainModels.add(domainModel);
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomains(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getSize()))
                .withSelfRel()
        );
        if (isValidPage(paginatedDomainsDto.getNumber(),paginatedDomainsDto.getTotalPages())) {
            if (isNotLastPage(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(paginatedDomainsDto.getNumber()+1, paginatedDomainsDto.getSize()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedDomainsDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(paginatedDomainsDto.getNumber()-1, paginatedDomainsDto.getSize()))
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

    private boolean isValidPage(int pageNumber, int totalPages) {
        return pageNumber+1 <= totalPages;
    }

    private boolean isNotFirstPage(int pageNumber) {
        return pageNumber != 0;
    }

    private boolean isNotLastPage(int pageNumber, int totalPages) {
        return (pageNumber + 1 != totalPages);
    }

    private CollectionModel<DomainCheckModel> convertToPagedDomainHistoricEntryModel(PaginatedDomainCheckDto paginatedDomainCheckDto, String domain) {
        var historicEntries = new ArrayList<DomainCheckModel>();
        for (var domainEntity : paginatedDomainCheckDto.getDomainChecks()) {
            historicEntries.add(convertToDomainHistoricEntry(domainEntity));
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

    private DomainCheckModel convertToDomainHistoricEntry(DomainCheckDto domainCheckDto) {
        var entry = new DomainCheckModel(
                domainCheckDto.getDomain(),
                domainCheckDto.getStatusCode(),
                domainCheckDto.getCertificateIsValid(),
                domainCheckDto.getCertificateExpiresOn(),
                domainCheckDto.getTimeCheckedOn(),
                domainCheckDto.getResponseTimeNs(),
                domainCheckDto.getDnsResolves()
        );
        entry.add(linkTo(methodOn(DomainsController.class).getDomainsHistoricEntry(
                domainCheckDto.getDomain(),
                domainCheckDto.getId())).withSelfRel()
        );
        return entry;
    }

    private DomainStatusModel convertToDomainStatusModel(DomainStatusDto domainStatusDto) {
        CertificateModel issuerCertificate = convertToCertificateModel(domainStatusDto.getIssuerCertificate());
        var caCertificates = new ArrayList<CertificateModel>();
        for (var certificate : domainStatusDto.getCaCertificates()) {
            caCertificates.add(convertToCertificateModel(certificate));
        }
        return new DomainStatusModel(
                caCertificates,
                domainStatusDto.getHostname(),
                domainStatusDto.getIpAddress(),
                domainStatusDto.getStatusCode(),
                domainStatusDto.getDnsResolved(),
                domainStatusDto.getResponseTimeNs(),
                issuerCertificate
        );

    }

    private CertificateModel convertToCertificateModel(CertificateDetailsDto certificateDetailsDto) {
        if (Optional.ofNullable(certificateDetailsDto).isEmpty()) {
            return null;
        }
        return new CertificateModel(
                certificateDetailsDto.getIssuedBy(),
                certificateDetailsDto.getIssuedFor(),
                certificateDetailsDto.getNotBefore(),
                certificateDetailsDto.getNotAfter(),
                certificateDetailsDto.isValid(),
                certificateDetailsDto.getExpired()
        );
    }
}