package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.model.*;
import com.bompotis.netcheck.api.model.assembler.*;
import com.bompotis.netcheck.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Created by Kyriakos Bompotis on 28/11/18.
 */
@RestController
@CrossOrigin(origins = {"${settings.cors.origin}"})
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
            @RequestParam(name = "size", required = false) Integer size) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return ok(new DomainModelAssembler().toCollectionModel(domainService.getPaginatedDomains(page,size)));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}")
    public ResponseEntity<DomainCheckModel> getDomainStatus(@PathVariable("domain") String domain) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        var status = domainService.check(domain);
        var domainCheckModel = new DomainCheckModelAssembler().toModel(status);
        domainCheckModel.add(linkTo(methodOn(DomainsController.class).getDomainStatus(domain)).withSelfRel());
        return ok(domainCheckModel);
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
        return ok(new DomainCheckModelAssembler().toCollectionModel(domainService.getDomainHistory(domain,page,size), domain));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/http")
    public ResponseEntity<CollectionModel<HttpCheckModel>> getHttpChecks(@PathVariable("domain") String domain,
                                                                         @RequestParam(name = "page", required = false) Integer page,
                                                                         @RequestParam(name = "size", required = false) Integer size) {
        return ok(new HttpCheckModelAssembler().toCollectionModel(domainService.getHttpDomainHistory(domain,page,size), domain, "http"));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/https")
    public ResponseEntity<CollectionModel<HttpCheckModel>> getHttpsChecks(@PathVariable("domain") String domain,
                                                                               @RequestParam(name = "page", required = false) Integer page,
                                                                               @RequestParam(name = "size", required = false) Integer size) {
        return ok(new HttpCheckModelAssembler().toCollectionModel(domainService.getHttpsDomainHistory(domain,page,size), domain, "https"));
    }


    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/metrics")
    public ResponseEntity<CollectionModel<MetricModel>> getDomainsMetrics(@PathVariable("domain") String domain,
                                                                          @RequestParam(name = "protocol") String protocol,
                                                                          @RequestParam(name = "period") String period,
                                                                          @RequestParam(name = "page", required = false) Integer page,
                                                                          @RequestParam(name = "size", required = false) Integer size) {
        return ok(new MetricModelAssembler().toCollectionModel(
                domainService.getDomainMetrics(
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

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/states")
    public ResponseEntity<CollectionModel<StateModel>> getDomainsStates(@PathVariable("domain") String domain,
                                                                        @RequestParam(name = "protocol", required = false) String protocol,
                                                                        @RequestParam(name = "page", required = false) Integer page,
                                                                        @RequestParam(name = "size", required = false) Integer size) {
        final var normalizedProtocol = Optional.ofNullable(protocol).orElse("HTTPS").toUpperCase();
        return ok(new StateModelAssembler().toCollectionModel(domainService.getDomainStates(domain, normalizedProtocol, page, size), domain, normalizedProtocol));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/history/{id}")
    public ResponseEntity<DomainCheckModel> getDomainsHistoricEntry(@PathVariable("domain") String domain, @PathVariable("id") String id) {
        var optionalEntity = domainService.getDomainCheck(domain,id);
        if (optionalEntity.isEmpty()) {
            return notFound().build();
        }
        return ok(new DomainCheckModelAssembler().toModel(optionalEntity.get()));
    }
}