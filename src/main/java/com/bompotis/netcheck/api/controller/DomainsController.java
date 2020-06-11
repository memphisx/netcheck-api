package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.models.CertificateModel;
import com.bompotis.netcheck.api.models.DomainStatusModel;
import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.api.models.Domain;
import com.bompotis.netcheck.api.models.DomainHistoricEntry;
import com.bompotis.netcheck.service.CertificateDetails;
import com.bompotis.netcheck.service.DomainService;
import com.bompotis.netcheck.service.DomainStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Created by Kyriakos Bompotis on 28/11/18.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/domains")
public class DomainsController {

    private final DomainRepository domainRepository;

    private final DomainService domainService;

    @Autowired
    public DomainsController(DomainRepository domainRepository, DomainService domainService) {
        this.domainRepository = domainRepository;
        this.domainService = domainService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public CollectionModel<Domain> getDomains() throws IOException {
        var collectionModel = CollectionModel.of(convertToDomainModel(domainRepository.findAll()));
        collectionModel.add(linkTo(methodOn(DomainsController.class).getDomains()).withSelfRel());
        return collectionModel;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}")
    public DomainStatusModel getDomainStatus(@PathVariable("url") String url, @RequestParam(name = "store", required = false) Boolean store) throws IOException {
        var status = domainService.buildAndCheck(url);
        if(Optional.ofNullable(store).isPresent() && store) {
            status.storeResult();
        }
        var domainStatusModel = convertToDomainStatusModel(status);
        domainStatusModel.add(linkTo(methodOn(DomainsController.class).getDomainStatus(url,store)).withSelfRel());
        return domainStatusModel;
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{url}")
    public ResponseEntity<Object> addDomainToScheduler(@PathVariable("url") String url) {
        domainService.scheduleDomainToCheck(url);
        return ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}/history")
    public CollectionModel<DomainHistoricEntry> getDomainsHistory(@PathVariable("url") String url) {
        var collectionModel = CollectionModel.of(convertToDomainHistoricEntryModel(domainService.getDomainHistory(url)));
        collectionModel.add(linkTo(methodOn(DomainsController.class).getDomainsHistory(url)).withSelfRel());
        return collectionModel;
    }

    private List<Domain> convertToDomainModel(Iterable<DomainEntity> domainEntities) throws IOException {
        ArrayList<Domain> domains = new ArrayList<>();
        for (DomainEntity domainEntity : domainEntities) {
            Domain domain = new Domain(domainEntity.getDomain());
            domain.add(linkTo(methodOn(DomainsController.class).getDomainStatus(domain.getDomain(),false)).withSelfRel());
            domains.add(domain);
        }
        return domains;
    }

    private List<DomainHistoricEntry> convertToDomainHistoricEntryModel(Iterable<DomainHistoricEntryEntity> domainEntities) {
        ArrayList<DomainHistoricEntry> historicEntries = new ArrayList<>();
        for (DomainHistoricEntryEntity domainEntity : domainEntities) {
            DomainHistoricEntry historicEntry = new DomainHistoricEntry(
                    domainEntity.getDomainEntity().getDomain(),
                    domainEntity.getStatusCode(),
                    domainEntity.getCertificateIsValid(),
                    domainEntity.getCertificateExpiresOn(),
                    domainEntity.getTimeCheckedOn(),
                    domainEntity.getDnsResolves()
            );
            historicEntries.add(historicEntry);
        }
        return historicEntries;
    }

    private DomainStatusModel convertToDomainStatusModel(DomainStatus domainStatus) {
        var issuerCertificate = convertToCertificateModel(domainStatus.getIssuerCertificate());
        var caCertificates = new ArrayList<CertificateModel>();
        for (var certificate : domainStatus.getCaCertificates()) {
            caCertificates.add(convertToCertificateModel(certificate));
        }
        return new DomainStatusModel(
                caCertificates,
                domainStatus.getHostname(),
                domainStatus.getIpAddress(),
                domainStatus.getStatusCode(),
                domainStatus.getDnsResolved(),
                issuerCertificate
        );

    }

    private CertificateModel convertToCertificateModel(CertificateDetails certificateDetails) {
        return new CertificateModel(
                certificateDetails.getIssuedBy(),
                certificateDetails.getIssuedFor(),
                certificateDetails.getNotBefore(),
                certificateDetails.getNotAfter(),
                certificateDetails.isValid(),
                certificateDetails.getExpired()
        );
    }
}