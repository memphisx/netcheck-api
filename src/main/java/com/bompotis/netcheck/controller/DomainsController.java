package com.bompotis.netcheck.controller;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoryEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.models.Domain;
import com.bompotis.netcheck.models.DomainHistoricEntry;
import com.bompotis.netcheck.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

    private final DomainHistoryEntryRepository domainHistoryEntryRepository;

    @Autowired
    public DomainsController(DomainRepository domainRepository, DomainHistoryEntryRepository domainHistoryEntryRepository) {
        this.domainRepository = domainRepository;
        this.domainHistoryEntryRepository = domainHistoryEntryRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public CollectionModel<Domain> getDomains() throws IOException, URISyntaxException {
        var collectionModel = CollectionModel.of(convertToDomainModel(domainRepository.findAll()));
        collectionModel.add(linkTo(methodOn(DomainsController.class).getDomains()).withSelfRel());
        return collectionModel;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}")
    public ResponseEntity getDomainStatus(@PathVariable("url") String url, @RequestParam(name = "store", required = false) Boolean store) throws IOException, URISyntaxException {
        var domain = new DomainService(url, domainRepository, domainHistoryEntryRepository);
        var result = domain.checkCerts(store);
        return ok(result);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}/history")
    public CollectionModel<DomainHistoricEntry> getDomainsHistory(@PathVariable("url") String url) throws IOException, URISyntaxException {
        var domain = new DomainService(url, domainRepository, domainHistoryEntryRepository);
        var collectionModel = CollectionModel.of(convertToDomainHistoricEntryModel(domain.getDomainHistory()));
        collectionModel.add(linkTo(methodOn(DomainsController.class).getDomainsHistory(url)).withSelfRel());
        return collectionModel;
    }

    private List<Domain> convertToDomainModel(Iterable<DomainEntity> domainEntities) throws IOException, URISyntaxException {
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
}