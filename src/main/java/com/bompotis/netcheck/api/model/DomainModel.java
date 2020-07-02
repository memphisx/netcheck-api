package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Relation(collectionRelation = "domains", itemRelation = "domain")
public class DomainModel extends RepresentationModel<DomainModel> {
    private final String domain;

    private final DomainCheckModel lastDomainCheck;

    private final Integer checkFrequencyMinutes;

    private final Date dateAdded;

    @JsonCreator
    public DomainModel(@JsonProperty("domain") String domain,
                       @JsonProperty("lastDomainCheck") DomainCheckModel lastDomainCheck,
                       @JsonProperty("dateAdded") Date dateAdded,
                       @JsonProperty("checkFrequencyMinutes") Integer checkFrequencyMinutes) {
        this.domain = domain;
        this.lastDomainCheck = lastDomainCheck;
        this.dateAdded = dateAdded;
        this.checkFrequencyMinutes = checkFrequencyMinutes;
    }

    public String getDomain() {
        return domain;
    }

    public DomainCheckModel getLastDomainCheckModel() {
        return lastDomainCheck;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public Integer getCheckFrequencyMinutes() {
        return checkFrequencyMinutes;
    }
}
