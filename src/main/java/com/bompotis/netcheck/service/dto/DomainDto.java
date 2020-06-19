package com.bompotis.netcheck.service.dto;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 19/6/20.
 */
public class DomainDto {
    private final String domain;
    private final DomainCheckDto lastDomainCheck;
    private final Date createdAt;

    public DomainCheckDto getLastDomainCheck() {
        return lastDomainCheck;
    }

    public String getDomain() {
        return domain;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private DomainCheckDto lastDomainCheck;
        private String domain;
        private Date createdAt;

        public Builder lastDomainCheck(DomainCheckDto lastDomainCheck) {
            this.lastDomainCheck = lastDomainCheck;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DomainDto build() {
            return new DomainDto(this);
        }
    }

    private DomainDto(Builder b) {
        this.lastDomainCheck = b.lastDomainCheck;
        this.domain = b.domain;
        this.createdAt = b.createdAt;
    }
}
