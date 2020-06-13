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
    private List<DomainHistoricEntryEntity> domainHistoryEntries;

    public List<DomainHistoricEntryEntity> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public void setDomainHistoryEntries(List<DomainHistoricEntryEntity> domainHistoryEntries) {
        this.domainHistoryEntries = domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
