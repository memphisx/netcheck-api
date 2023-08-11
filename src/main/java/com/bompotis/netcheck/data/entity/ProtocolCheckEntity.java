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
package com.bompotis.netcheck.data.entity;

import org.springframework.lang.NonNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;

import java.util.Objects;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "protocol_check")
public class ProtocolCheckEntity extends AbstractTimestampablePersistable<String>{

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "protocol")
    private Protocol protocol;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "connection_accepted")
    private boolean connectionAccepted;

    @Column(name = "dns_resolves")
    private boolean dnsResolves;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "redirect_uri")
    private String redirectUri;

    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean getDnsResolves() {
        return dnsResolves;
    }

    @NonNull
    public Protocol getProtocol() {
        return protocol;
    }

    public String getHostname() {
        return hostname;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public boolean isConnectionAccepted() {
        return connectionAccepted;
    }

    public enum Protocol {
        HTTP,
        HTTPS
    }

    public static class Builder {
        private Protocol protocol;
        private Integer statusCode;
        private boolean dnsResolves;
        private boolean connectionAccepted;
        private String hostname;
        private String redirectUri;

        public Builder protocol(String protocol) {
            if (protocol.equals("HTTPS")) {
                this.protocol = Protocol.HTTPS;
            }
            if (protocol.equals("HTTP")) {
                this.protocol = Protocol.HTTP;
            }
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder connectionAccepted(boolean connectionAccepted) {
            this.connectionAccepted = connectionAccepted;
            return this;
        }

        public Builder dnsResolves(boolean dnsResolves) {
            this.dnsResolves = dnsResolves;
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

        public ProtocolCheckEntity build() {
            return new ProtocolCheckEntity(this);
        }
    }

    protected ProtocolCheckEntity() {}

    private ProtocolCheckEntity(Builder b) {
        this.protocol = b.protocol;
        this.statusCode = b.statusCode;
        this.dnsResolves = b.dnsResolves;
        this.hostname = b.hostname;
        this.redirectUri = b.redirectUri;
        this.connectionAccepted = b.connectionAccepted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtocolCheckEntity that = (ProtocolCheckEntity) o;
        return dnsResolves == that.dnsResolves &&
                protocol == that.protocol &&
                Objects.equals(statusCode, that.statusCode) &&
                Objects.equals(connectionAccepted, that.connectionAccepted) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, connectionAccepted, statusCode, dnsResolves, hostname, redirectUri);
    }
}
