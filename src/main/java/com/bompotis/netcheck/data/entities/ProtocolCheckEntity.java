package com.bompotis.netcheck.data.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "protocol_check")
@Inheritance(strategy=InheritanceType.JOINED)
public class ProtocolCheckEntity {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }


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
