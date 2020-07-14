package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.model.*;
import com.bompotis.netcheck.api.model.assembler.*;
import com.bompotis.netcheck.service.DomainService;
import com.bompotis.netcheck.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    private final MetricService metricService;

    @Autowired
    public DomainsController(DomainService domainService, MetricService metricService) {
        this.domainService = domainService;
        this.metricService = metricService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<CollectionModel<DomainModel>> getDomains(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return ok(new DomainModelAssembler().toCollectionModel(domainService.getPaginatedDomains(page,size)));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}")
    public ResponseEntity<DomainModel> getDomainStatus(@PathVariable("domain") String domain) {
        var result = new AtomicReference<ResponseEntity<DomainModel>>();
        domainService.getDomain(domain).ifPresentOrElse(
                domainDto -> result.set(ok(new DomainModelAssembler().toModel(domainDto))),
                () -> result.set(notFound().build())
        );
        return result.get();
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{domain}")
    public ResponseEntity<Object> addDomainToScheduler(@PathVariable("domain") String domain,
                                                       @RequestParam(name = "frequency", required = false) Integer frequency) {
        domainService.scheduleDomainToCheck(domain, frequency);
        return ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/history")
    public ResponseEntity<CollectionModel<DomainCheckModel>> getDomainsHistory(@PathVariable("domain") String domain,
                                                                               @RequestParam(name = "page", required = false) Integer page,
                                                                               @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new DomainCheckModelAssembler().toCollectionModel(domainService.getDomainHistory(domain, page, size), domain));
        }
        return notFound().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/http")
    public ResponseEntity<CollectionModel<HttpCheckModel>> getHttpChecks(@PathVariable("domain") String domain,
                                                                         @RequestParam(name = "page", required = false) Integer page,
                                                                         @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new HttpCheckModelAssembler().toCollectionModel(domainService.getHttpDomainHistory(domain,page,size), domain, "http"));
        }
        return notFound().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/https")
    public ResponseEntity<CollectionModel<HttpCheckModel>> getHttpsChecks(@PathVariable("domain") String domain,
                                                                               @RequestParam(name = "page", required = false) Integer page,
                                                                               @RequestParam(name = "size", required = false) Integer size) {
        if (domainService.domainIsScheduled(domain)) {
            return ok(new HttpCheckModelAssembler().toCollectionModel(domainService.getHttpsDomainHistory(domain,page,size), domain, "https"));
        }
        return notFound().build();
    }


    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/metrics")
    public ResponseEntity<CollectionModel<MetricModel>> getDomainsMetrics(@PathVariable("domain") String domain,
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

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/states")
    public ResponseEntity<CollectionModel<StateModel>> getDomainsStates(@PathVariable("domain") String domain,
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

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}/history/{id}")
    public ResponseEntity<DomainCheckModel> getDomainsHistoricEntry(@PathVariable("domain") String domain, @PathVariable("id") String id) {
        var result = new AtomicReference<ResponseEntity<DomainCheckModel>>();
        domainService.getDomainCheck(domain,id).ifPresentOrElse(
                domainCheck -> result.set(ok(new DomainCheckModelAssembler().toModel(domainCheck))),
                () -> result.set(notFound().build())
        );
        return result.get();
    }
}