/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Relation(collectionRelation = "domains", itemRelation = "domain")
public class DomainResponse extends RepresentationModel<DomainResponse> {
    private final String domain;

    private final DomainCheckModel lastDomainCheck;

    private final Integer checkFrequencyMinutes;

    private final Date dateAdded;

    private final String endpoint;

    private final Map<String,String> headers;

    private final Integer timeoutMs;

    @JsonCreator
    public DomainResponse(@JsonProperty("domain") String domain,
                          @JsonProperty("lastDomainCheck") DomainCheckModel lastDomainCheck,
                          @JsonProperty("dateAdded") Date dateAdded,
                          @JsonProperty("checkFrequencyMinutes") Integer checkFrequencyMinutes,
                          @JsonProperty("endpoint") String endpoint,
                          @JsonProperty("headers") Map<String,String> headers,
                          @JsonProperty("timeoutMs") Integer timeoutMs) {
        this.domain = domain;
        this.lastDomainCheck = lastDomainCheck;
        this.dateAdded = dateAdded;
        this.checkFrequencyMinutes = checkFrequencyMinutes;
        this.endpoint = endpoint;
        this.headers = headers;
        this.timeoutMs = timeoutMs;
    }

    public String getDomain() {
        return domain;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DomainCheckModel getLastChecks() {
        return lastDomainCheck;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public Integer getCheckFrequencyMinutes() {
        return checkFrequencyMinutes;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }
}
