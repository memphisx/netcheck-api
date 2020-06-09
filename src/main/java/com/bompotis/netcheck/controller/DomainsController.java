package com.bompotis.netcheck.controller;

import com.bompotis.netcheck.data.repositories.DomainHistoryEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.service.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

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
    public ResponseEntity getDomains() {
        return ok(domainRepository.findAll());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}")
    public ResponseEntity getDomainStatus(@PathVariable("url") String url, @RequestParam(name = "store", required = false) Boolean store) throws IOException, URISyntaxException {
        var domain = new Domain(url, domainRepository, domainHistoryEntryRepository);
        var result = domain.checkCerts(store);
        return ok(result);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}/history")
    public ResponseEntity getDomainsHistory(@PathVariable("url") String url) throws IOException, URISyntaxException {
        var domain = new Domain(url, domainRepository, domainHistoryEntryRepository);
        return ok(domain.getDomainHistory());
    }

}
