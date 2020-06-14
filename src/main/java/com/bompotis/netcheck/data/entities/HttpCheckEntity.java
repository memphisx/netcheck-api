package com.bompotis.netcheck.data.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "http_check")
@Inheritance(strategy=InheritanceType.JOINED)
public class HttpCheckEntity {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "dns_resolves")
    private Boolean dnsResolves;

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
}
