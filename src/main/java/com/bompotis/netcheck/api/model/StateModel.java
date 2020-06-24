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

    @JsonCreator
    public StateModel(
            @JsonProperty("hostname") String hostname,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("checkedOn") Date timeCheckedOn,
            @JsonProperty("reason") String reason,
            @JsonProperty("dnsResolves") Boolean dnsResolves,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("redirectUri") String redirectUri,
            @JsonProperty("isUp") Boolean isUp) {
        this.isUp = isUp;
        this.hostname = hostname;
        this.statusCode = statusCode;
        this.timeCheckedOn = timeCheckedOn;
        this.reason = reason;
        this.dnsResolves = dnsResolves;
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
}
