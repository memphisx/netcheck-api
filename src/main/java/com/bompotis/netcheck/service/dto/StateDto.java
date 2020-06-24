package com.bompotis.netcheck.service.dto;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Kyriakos Bompotis on 24/6/20.
 */
public class StateDto {
    private final String id;
    private final Integer statusCode;
    private final Date timeCheckedOn;
    private final Boolean dnsResolves;
    private final String hostname;
    private final String protocol;
    private final String redirectUri;
    private final boolean certificatesChange;
    private final boolean httpCheckChange;
    private final boolean httpsCheckChange;

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getHostname() {
        return hostname;
    }

    public String getId() {
        return id;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public Boolean isUp() {
        AtomicBoolean result = new AtomicBoolean(false);
        Optional.ofNullable(statusCode).ifPresent(
                (code) -> {
                    if (code < 400 ) {
                        result.set(true);
                    }
                }
        );
        return result.get();
    }

    public String getReason() {
        if (certificatesChange && httpCheckChange && httpsCheckChange) {
            return "New Entry";
        }
        if (protocol.equals("HTTPS") && certificatesChange) {
            return "Certificates Changed";
        }
        if (!dnsResolves) {
            return "Domain name cannot be resolved";
        }
        if (Optional.ofNullable(redirectUri).isPresent()) {
            return "Website redirects. HTTP RESPONSE STATUS CODE " + statusCode;
        }
        if (!isUp()) {
            return "Website is down. HTTP RESPONSE STATUS CODE " + statusCode;
        }
        return "Website went UP";

    }

    public static class Builder {
        private String id;
        private Integer statusCode;
        private Date timeCheckedOn;
        private Boolean dnsResolves;
        private String hostname;
        private String protocol;
        private String redirectUri;
        private boolean certificatesChange;
        private boolean httpCheckChange;
        private boolean httpsCheckChange;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder timeCheckedOn(Date timeCheckedOn) {
            this.timeCheckedOn = timeCheckedOn;
            return this;
        }

        public Builder dnsResolves(Boolean dnsResolves) {
            this.dnsResolves = dnsResolves;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder certificatesChange(boolean certificatesChange) {
            this.certificatesChange = certificatesChange;
            return this;
        }

        public Builder httpCheckChange(boolean httpCheckChange) {
            this.httpCheckChange = httpCheckChange;
            return this;
        }

        public Builder httpsCheckChange(boolean httpsCheckChange) {
            this.httpsCheckChange = httpsCheckChange;
            return this;
        }

        public StateDto build() {
            return new StateDto(this);
        }
    }

    private StateDto(Builder b) {
        this.id = b.id;
        this.statusCode = b.statusCode;
        this.timeCheckedOn = b.timeCheckedOn;
        this.dnsResolves = b.dnsResolves;
        this.hostname = b.hostname;
        this.protocol = b.protocol;
        this.redirectUri = b.redirectUri;
        this.certificatesChange = b.certificatesChange;
        this.httpCheckChange = b.httpCheckChange;
        this.httpsCheckChange = b.httpsCheckChange;
    }
}
