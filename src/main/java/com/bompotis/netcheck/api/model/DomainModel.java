package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Relation(collectionRelation = "domains", itemRelation = "domain")
public class DomainModel extends RepresentationModel<DomainModel> {
    private final String domain;

    @JsonCreator
    public DomainModel(@JsonProperty("domain") String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
