package com.bompotis.netcheck.data.entity;

import org.springframework.lang.NonNull;

import javax.persistence.*;
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

    @NonNull
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

    public Protocol getProtocol() {
        return protocol;
    }

    public String getHostname() {
        return hostname;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public enum Protocol {
        HTTP,
        HTTPS
    }

    public static class Builder {
        private Protocol protocol;
        private Integer statusCode;
        private boolean dnsResolves;
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtocolCheckEntity that = (ProtocolCheckEntity) o;
        return dnsResolves == that.dnsResolves &&
                protocol == that.protocol &&
                Objects.equals(statusCode, that.statusCode) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, statusCode, dnsResolves, hostname, redirectUri);
    }
}
