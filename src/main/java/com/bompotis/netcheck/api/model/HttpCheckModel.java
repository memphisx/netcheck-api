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
 * Created by Kyriakos Bompotis on 15/6/20.
 */
@Relation(collectionRelation = "checks", itemRelation = "check")
public class HttpCheckModel extends RepresentationModel<HttpCheckModel> {
    private final Boolean isUp;
    private final Integer statusCode;
    private final Date timeCheckedOn;
    private final Boolean dnsResolves;
    private final String hostname;
    private final String ipAddress;
    private final Long responseTimeNs;
    private final String protocol;
    private final String redirectUri;
    private final String errorMessage;

    @JsonCreator
    public HttpCheckModel(
            @JsonProperty("hostname") String hostname,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("checkedOn") Date timeCheckedOn,
            @JsonProperty("responseTimeNs") Long responseTimeNs,
            @JsonProperty("dnsResolves") Boolean dnsResolves,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("redirectUri") String redirectUri,
            @JsonProperty("up") Boolean isUp,
            @JsonProperty("errorMessage") String errorMessage) {
        this.isUp = isUp;
        this.hostname = hostname;
        this.statusCode = statusCode;
        this.timeCheckedOn = timeCheckedOn;
        this.responseTimeNs = responseTimeNs;
        this.dnsResolves = dnsResolves;
        this.ipAddress = ipAddress;
        this.protocol = protocol;
        this.redirectUri = redirectUri;
        this.errorMessage = errorMessage;
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

    public Long getResponseTimeNs() {
        return responseTimeNs;
    }

    public String getIpAddress() {
        return ipAddress;
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

    public String getErrorMessage() {
        return errorMessage;
    }
}
