package com.bompotis.netcheck.data.entity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain")
public class DomainEntity extends AbstractTimestampable<String>{

    @Id
    @Column(name = "domain")
    private String domain;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="domainEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DomainCheckEntity> domainHistoryEntries;

    public Set<DomainCheckEntity> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public void setDomainHistoryEntries(Set<DomainCheckEntity> domainHistoryEntries) {
        this.domainHistoryEntries = domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getId()  {
        return domain;
    }

    @Override
    public boolean isNew() {
        return null == domain;
    }
}
