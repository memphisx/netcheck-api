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

/**
 * Created by Kyriakos Bompotis on 24/6/20.
 */
@Relation(collectionRelation = "states", itemRelation = "state")
public class StateModel extends RepresentationModel<StateModel> {
    private final Boolean isUp;
    private final Integer statusCode;
    private final Date timeCheckedOn;
    private final Boolean dnsResolves;
    private final String hostname;
    private final String protocol;
    private final String redirectUri;
    private final String reason;
    private final Long durationSeconds;

    @JsonCreator
    public StateModel(
            @JsonProperty("hostname") String hostname,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("checkedOn") Date timeCheckedOn,
            @JsonProperty("reason") String reason,
            @JsonProperty("dnsResolves") Boolean dnsResolves,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("redirectUri") String redirectUri,
            @JsonProperty("isUp") Boolean isUp,
            @JsonProperty("durationSeconds") Long durationSeconds) {
        this.isUp = isUp;
        this.hostname = hostname;
        this.statusCode = statusCode;
        this.timeCheckedOn = timeCheckedOn;
        this.reason = reason;
        this.dnsResolves = dnsResolves;
        this.protocol = protocol;
        this.redirectUri = redirectUri;
        this.durationSeconds = durationSeconds;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public String getHostname() {
        return hostname;
    }

    public String getProtocol() {
        return protocol;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getRedirectUri() {
        return redirectUri;
    }

    public Boolean getUp() {
        return isUp;
    }

    public String getReason() {
        return reason;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }
}
