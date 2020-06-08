package com.bompotis.netcheck.controller;

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

    @Autowired
    public DomainsController() {
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{url}")
    public ResponseEntity getDomains(@PathVariable("url") String url) throws IOException, URISyntaxException {
        var domain = new Domain("https://" + url);
        return ok(domain.checkCerts());
    }

}
