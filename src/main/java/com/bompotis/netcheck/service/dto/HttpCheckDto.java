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
package com.bompotis.netcheck.service.dto;

import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Kyriakos Bompotis on 15/6/20.
 */
public class HttpCheckDto {
    private final String id;
    private final Long responseTimeNs;
    private final String hostname;
    private final Integer statusCode;
    private final Boolean dnsResolved;
    private final Boolean connectionAccepted;
    private final String ipAddress;
    private final Date timeCheckedOn;
    private final String protocol;
    private final String redirectUri;

    public Integer getStatusCode() {
        return statusCode;
    }

    public Boolean getDnsResolved() {
        return dnsResolved;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public Long getResponseTimeNs() {
        return responseTimeNs;
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

    public Boolean getConnectionAccepted() {
        return connectionAccepted;
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

    public static class Builder {
        private Long responseTimeNs;
        private String hostname;
        private Integer statusCode;
        private Boolean dnsResolved;
        private Boolean connectionAccepted;
        private String ipAddress;
        private String id;
        private Date timeCheckedOn;
        private String protocol;
        private String redirectUri;

        public Builder dnsResolved(Boolean dnsResolved) {
            this.dnsResolved = dnsResolved;
            return this;
        }

        public Builder connectionAccepted(Boolean connectionAccepted) {
            this.connectionAccepted = connectionAccepted;
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

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public HttpCheckDto build() {
            return new HttpCheckDto(this);
        }
    }

    private HttpCheckDto(Builder b) {
        this.ipAddress = b.ipAddress;
        this.hostname = b.hostname;
        this.dnsResolved = b.dnsResolved;
        this.responseTimeNs = b.responseTimeNs;
        this.statusCode = b.statusCode;
        this.timeCheckedOn = b.timeCheckedOn;
        this.protocol = b.protocol;
        this.id = b.id;
        this.redirectUri = b.redirectUri;
        this.connectionAccepted = b.connectionAccepted;
    }

    public HttpCheckDto(ProtocolCheckEntity entity, Long responseTimeNs, Date timeCheckedOn, String ipAddress) {
        this.statusCode = entity.getStatusCode();
        this.id = entity.getId();
        this.hostname = entity.getHostname();
        this.protocol = entity.getProtocol().toString();
        this.ipAddress = ipAddress;
        this.redirectUri = entity.getRedirectUri();
        this.responseTimeNs = responseTimeNs;
        this.timeCheckedOn = timeCheckedOn;
        this.dnsResolved = entity.getDnsResolves();
        this.connectionAccepted = entity.isConnectionAccepted();
    }

    public ProtocolCheckEntity toProtocolCheckEntity() {
        return new ProtocolCheckEntity.Builder()
                .dnsResolves(this.getDnsResolved())
                .statusCode(this.getStatusCode())
                .connectionAccepted(this.getConnectionAccepted())
                .protocol(this.getProtocol())
                .hostname(this.getHostname())
                .redirectUri(this.getRedirectUri())
                .build();
    }
}
