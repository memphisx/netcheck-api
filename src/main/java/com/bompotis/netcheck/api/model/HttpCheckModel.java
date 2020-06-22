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
    private final Integer statusCode;
    private final Date timeCheckedOn;
    private final Boolean dnsResolves;
    private final String hostname;
    private final String ipAddress;
    private final Long responseTimeNs;
    private final String protocol;
    private final String redirectUri;

    @JsonCreator
    public HttpCheckModel(
            @JsonProperty("hostname") String hostname,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("checkedOn") Date timeCheckedOn,
            @JsonProperty("responseTimeNs") Long responseTimeNs,
            @JsonProperty("dnsResolves") Boolean dnsResolves,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("redirectUri") String redirectUri) {
        this.hostname = hostname;
        this.statusCode = statusCode;
        this.timeCheckedOn = timeCheckedOn;
        this.responseTimeNs = responseTimeNs;
        this.dnsResolves = dnsResolves;
        this.ipAddress = ipAddress;
        this.protocol = protocol;
        this.redirectUri = redirectUri;
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
}
