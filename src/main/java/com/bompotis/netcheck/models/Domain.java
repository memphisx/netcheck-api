package com.bompotis.netcheck.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
public class Domain extends RepresentationModel<Domain> {
    private final String domain;

    @JsonCreator
    public Domain(@JsonProperty("domain") String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
