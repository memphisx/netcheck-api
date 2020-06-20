package com.bompotis.netcheck.data.entity;

import javax.persistence.*;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "protocol_check")
public class ProtocolCheckEntity extends AbstractTimestampablePersistable<String>{

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol")
    private Protocol protocol;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "dns_resolves")
    private Boolean dnsResolves;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "hostname")
    private String hostname;

    public Integer getStatusCode() {
        return statusCode;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public enum Protocol {
        HTTP,
        HTTPS
    }

    public static class Builder {
        private Protocol protocol;
        private Integer statusCode;
        private Boolean dnsResolves;
        private String ipAddress;
        private String hostname;

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

        public Builder dnsResolves(Boolean dnsResolves) {
            this.dnsResolves = dnsResolves;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
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
        this.ipAddress = b.ipAddress;
        this.hostname = b.hostname;
    }
}
