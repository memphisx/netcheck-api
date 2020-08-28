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
package com.bompotis.netcheck.scheduler.batch.notification;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
public class NotificationDto {

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

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

    private final Map<String, Object> currentState;

    private final Map<String, Object> previousState;

    private final String message;

    private final Status status;

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getHostname() {
        return hostname;
    }

    public Boolean isDnsResolves() {
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

    public Map<String, Object> getPreviousState() {
        return previousState;
    }

    public Map<String, Object> getCurrentState() {
        return currentState;
    }

    public Boolean getRootCertificatesChanged() {
        return rootCertificatesChanged;
    }

    public static class Builder {
        private final Set<String> STATE_PROPERTIES_BLACKLIST = Set.of("id", "createdAt", "updatedAt", "new");
        private Type type;
        private Integer statusCode;
        private Boolean dnsResolves;
        private Boolean connectionAccepted;
        private Boolean issuerCertificateHasExpired;
        private Boolean issuerCertificateIsValid;
        private Date issuerCertificateExpirationDate;
        private Boolean rootCertificatesChanged;
        private Map<String, Object> currentState;
        private Map<String, Object> previousState;
        private String redirectUri;
        private String hostname;
        private Date timeCheckedOn;
        private Long responseTimeNs;
        private String ipAddress;
        private final ObjectMapper oMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        private Map<String, Object> convertToMap(Object state) {
            var map = oMapper.convertValue(state, Map.class);
            map.keySet().removeAll(STATE_PROPERTIES_BLACKLIST);
            return map;
        }

        public Builder currentState(Object currentState) {
            this.currentState = Optional.ofNullable(currentState).isPresent() ? convertToMap(currentState) : null;
            return this;
        }

        public Builder previousState(Object previousState) {
            this.previousState = Optional.ofNullable(previousState).isPresent() ? convertToMap(previousState) : null;
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

        private Status getStatus() {
            boolean isUp = Optional.ofNullable(this.statusCode).orElse(1000) < 400;
            return isUp ? Status.UP : Status.DOWN;
        }
        private String getMessage() {
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
        this.message = b.getMessage();
        this.status = b.getStatus();
    }
}
