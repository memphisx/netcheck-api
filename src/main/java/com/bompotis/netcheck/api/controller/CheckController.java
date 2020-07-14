package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.model.DomainCheckModel;
import com.bompotis.netcheck.api.model.assembler.DomainCheckModelAssembler;
import com.bompotis.netcheck.service.DomainService;
import com.bompotis.netcheck.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = "/check")
public class CheckController {

    private final DomainService domainService;

    @Autowired
    public CheckController(DomainService domainService) {
        this.domainService = domainService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{domain}")
    public ResponseEntity<DomainCheckModel> getDomainStatus(@PathVariable("domain") String domain) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        var status = domainService.check(domain);
        var domainCheckModel = new DomainCheckModelAssembler().toModel(status);
        domainCheckModel.add(linkTo(methodOn(DomainsController.class).getDomainStatus(domain)).withSelfRel());
        return ok(domainCheckModel);
    }
}
