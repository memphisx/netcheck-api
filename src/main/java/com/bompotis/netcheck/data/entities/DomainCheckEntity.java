package com.bompotis.netcheck.data.entities;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain_check")
public class DomainCheckEntity {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private HttpCheckEntity httpCheckEntity;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private HttpsCheckEntity httpsCheckEntity;

    @Column(name = "domain", insertable = false, updatable = false)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "domain")
    private DomainEntity domainEntity;

    @Column(name = "check_date")
    private Date timeCheckedOn;

    @Column(name = "https_response_time_ns")
    private Long httpsResponseTimeNs;

    public Long getHttpsResponseTimeNs() {
        return httpsResponseTimeNs;
    }

    public void setHttpsResponseTimeNs(Long responseTimeNs) {
        this.httpsResponseTimeNs = responseTimeNs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HttpCheckEntity getHttpCheckEntity() {
        return httpCheckEntity;
    }

    public void setHttpCheckEntity(HttpCheckEntity httpCheckEntity) {
        this.httpCheckEntity = httpCheckEntity;
    }

    public HttpsCheckEntity getHttpsCheckEntity() {
        return httpsCheckEntity;
    }

    public void setHttpsCheckEntity(HttpsCheckEntity httpsCheckEntity) {
        this.httpsCheckEntity = httpsCheckEntity;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public DomainEntity getDomainEntity() {
        return domainEntity;
    }

    public void setDomainEntity(DomainEntity domainEntity) {
        this.domainEntity = domainEntity;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public void setTimeCheckedOn(Date timeCheckedOn) {
        this.timeCheckedOn = timeCheckedOn;
    }
}
