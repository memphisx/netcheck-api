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

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public void setDnsResolves(Boolean dnsResolves) {
        this.dnsResolves = dnsResolves;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setProtocol(String protocol) {
        if (protocol.equals("HTTPS")) {
            this.protocol = Protocol.HTTPS;
        }
        if (protocol.equals("HTTP")) {
            this.protocol = Protocol.HTTP;
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public enum Protocol {
        HTTP,
        HTTPS
    }
}
