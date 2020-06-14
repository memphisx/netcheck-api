package com.bompotis.netcheck.data.entities;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain")
public class DomainEntity {

    @Id
    @Column(name = "domain")
    private String domain;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="domainEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DomainCheckEntity> domainHistoryEntries;

    public List<DomainCheckEntity> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public void setDomainHistoryEntries(List<DomainCheckEntity> domainHistoryEntries) {
        this.domainHistoryEntries = domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
