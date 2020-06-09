package com.bompotis.netcheck.data.entities;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
public class DomainEntity {

    @Id
    private String domain;

    @OneToMany(fetch = FetchType.LAZY)
    private List<DomainHistoryEntry> domainHistoryEntries;

    public List<DomainHistoryEntry> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public void setDomainHistoryEntries(List<DomainHistoryEntry> domainHistoryEntries) {
        this.domainHistoryEntries = domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
