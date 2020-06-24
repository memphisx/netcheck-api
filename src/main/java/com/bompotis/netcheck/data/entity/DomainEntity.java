package com.bompotis.netcheck.data.entity;

import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.util.Objects;
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

    @NonNull
    @Column(name = "check_frequency_minutes")
    private int checkFrequency;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="domainEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DomainCheckEntity> domainHistoryEntries;

    protected DomainEntity() {}

    public Set<DomainCheckEntity> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public int getCheckFrequency() {
        return checkFrequency;
    }

    @Override
    public String getId()  {
        return domain;
    }

    @Override
    public boolean isNew() {
        return null == domain;
    }

    public static class Builder {
        private String domain;
        private Set<DomainCheckEntity> domainHistoryEntries;
        private int frequency = 10;

        public Builder domainHistoryEntries(Set<DomainCheckEntity> domainHistoryEntries) {
            this.domainHistoryEntries = domainHistoryEntries;
            return this;
        }

        public Builder frequency(int frequencyMinutes) {
            this.frequency = frequencyMinutes;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public DomainEntity build() {
            return new DomainEntity(this);
        }
    }

    private DomainEntity(Builder b) {
        this.domain = b.domain;
        this.domainHistoryEntries = b.domainHistoryEntries;
        this.checkFrequency = b.frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEntity that = (DomainEntity) o;
        return domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }
}
