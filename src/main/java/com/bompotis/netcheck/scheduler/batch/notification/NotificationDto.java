package com.bompotis.netcheck.scheduler.batch.notification;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
public class NotificationDto {

    public enum Status {
        UP,
        DOWN
    }

    public enum Type {
        HTTP,
        HTTPS,
        CERTIFICATE
    }

    private final Type type;

    private final String hostname;

    private final Integer statusCode;

    private final Boolean dnsResolves;

    private final String redirectUri;

    private final String ipAddress;

    private final boolean connectionAccepted;

    private final Date timeCheckedOn;

    private final Long responseTimeNs;

    private final Boolean issuerCertificateHasExpired;

    private final Boolean issuerCertificateIsValid;

    private final Date issuerCertificateExpirationDate;

    private final Boolean rootCertificatesChanged;

    private final JsonObject currentState;

    private final JsonObject previousState;

    public String getMessage() {
        switch (type) {
            case CERTIFICATE:
                if (issuerCertificateHasExpired) {
                    return String.format(
                            "Certificates for %s has expired. New Expiration date: %s",
                            hostname,
                            issuerCertificateExpirationDate
                    );
                } else if (!issuerCertificateIsValid) {
                    return String.format(
                            "Certificates for %s have changed. New certificate is invalid. Expiration Date: %s",
                            hostname,
                            issuerCertificateExpirationDate.toString()
                    );
                } else {
                    return String.format(
                            "Certificates for %s have changed. New Expiration date: %s",
                            hostname,
                            issuerCertificateExpirationDate.toString()
                    );
                }
            case HTTP:
            case HTTPS:
            default:
                return String.format(
                        "%s State for %s has changed to %s with status code %s",
                        type.name(),
                        hostname,
                        getStatus().name(),
                        statusCode);
        }
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getHostname() {
        return hostname;
    }

    public Status getStatus() {
        boolean isUp = Optional.ofNullable(this.getStatusCode()).orElse(1000) < 400;
        return isUp ? Status.UP : Status.DOWN;
    }

    public boolean isDnsResolves() {
        return dnsResolves;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public boolean isConnectionAccepted() {
        return connectionAccepted;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public Long getResponseTimeNs() {
        return responseTimeNs;
    }

    public Type getType() {
        return type;
    }

    public JsonObject getPreviousState() {
        return previousState;
    }

    public JsonObject getCurrentState() {
        return currentState;
    }

    public Boolean getRootCertificatesChanged() {
        return rootCertificatesChanged;
    }

    public static class Builder {
        private Type type;
        private Integer statusCode;
        private Boolean dnsResolves;
        private Boolean connectionAccepted;
        private Boolean issuerCertificateHasExpired;
        private Boolean issuerCertificateIsValid;
        private Date issuerCertificateExpirationDate;
        private Boolean rootCertificatesChanged;
        private JsonObject currentState;
        private JsonObject previousState;
        private String redirectUri;
        private String hostname;
        private Date timeCheckedOn;
        private Long responseTimeNs;
        private String ipAddress;

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder currentState(Object currentState) {
            this.currentState = new Gson().toJsonTree(currentState).getAsJsonObject();
            return this;
        }

        public Builder previousState(Object previousState) {
            this.previousState = new Gson().toJsonTree(previousState).getAsJsonObject();
            return this;
        }

        public Builder issuerCertificateIsValid(Boolean issuerCertificateIsValid) {
            this.issuerCertificateIsValid = issuerCertificateIsValid;
            return this;
        }

        public Builder rootCertificatesChanged(Boolean rootCertificatesChanged) {
            this.rootCertificatesChanged = rootCertificatesChanged;
            return this;
        }
        public Builder issuerCertificateHasExpired(Boolean issuerCertificateHasExpired) {
            this.issuerCertificateHasExpired = issuerCertificateHasExpired;
            return this;
        }

        public Builder issuerCertificateExpirationDate(Date issuerCertificateExpirationDate) {
            this.issuerCertificateExpirationDate = issuerCertificateExpirationDate;
            return this;
        }

        public Builder dnsResolves(Boolean dnsResolves) {
            this.dnsResolves = dnsResolves;
            return this;
        }

        public Builder connectionAccepted(Boolean connectionAccepted) {
            this.connectionAccepted = connectionAccepted;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder timeCheckedOn(Date timeCheckedOn) {
            this.timeCheckedOn = timeCheckedOn;
            return this;
        }

        public Builder responseTimeNs(Long responseTimeNs) {
            this.responseTimeNs = responseTimeNs;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public NotificationDto build() {
            return new NotificationDto(this);
        }
    }

    private NotificationDto(Builder b) {
        this.statusCode = b.statusCode;
        this.dnsResolves = b.dnsResolves;
        this.hostname = b.hostname;
        this.redirectUri = b.redirectUri;
        this.connectionAccepted = b.connectionAccepted;
        this.ipAddress = b.ipAddress;
        this.responseTimeNs = b.responseTimeNs;
        this.timeCheckedOn = b.timeCheckedOn;
        this.type = b.type;
        this.issuerCertificateExpirationDate = b.issuerCertificateExpirationDate;
        this.issuerCertificateHasExpired = b.issuerCertificateHasExpired;
        this.issuerCertificateIsValid = b.issuerCertificateIsValid;
        this.rootCertificatesChanged = b.rootCertificatesChanged;
        this.currentState = b.currentState;
        this.previousState = b.previousState;
    }
}
