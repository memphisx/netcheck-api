package com.bompotis.netcheck.service.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainStatusDto {
    private final List<CertificateDetailsDto> caCertificates;
    private final CertificateDetailsDto issuerCertificate;
    private final Long responseTimeNs;
    private final String domain;
    private final String hostname;
    private final Integer statusCode;
    private final Boolean dnsResolved;
    private final String ipAddress;

    public List<CertificateDetailsDto> getCaCertificates() {
        return caCertificates;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public CertificateDetailsDto getIssuerCertificate() {
        return issuerCertificate;
    }

    public Boolean getDnsResolved() {
        return dnsResolved;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public Long getResponseTimeNs() {
        return responseTimeNs;
    }

    public String getDomain() {
        return domain;
    }

    public static class Builder {
        private List<CertificateDetailsDto> caCertificates = new ArrayList<>();
        private CertificateDetailsDto issuerCertificate;
        private Long responseTimeNs;
        private String domain;
        private String hostname;
        private Integer statusCode;
        private Boolean dnsResolved;
        private String ipAddress;

        public Builder certificate(CertificateDetailsDto cert) {
            if (cert.getBasicConstraints() < 0) {
                this.issuerCertificate = cert;
            } else {
                this.caCertificates.add(cert);
            }
            return this;
        }

        public Builder dnsResolved(Boolean dnsResolved) {
            this.dnsResolved = dnsResolved;
            return this;
        }

        public Builder responseTimeNs(Long responseTimeNs) {
            this.responseTimeNs = responseTimeNs;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public DomainStatusDto build() {
            return new DomainStatusDto(this);
        }
    }

    private DomainStatusDto(Builder b) {
        this.ipAddress = b.ipAddress;
        this.domain = b.domain;
        this.hostname = b.hostname;
        this.dnsResolved = b.dnsResolved;
        this.responseTimeNs = b.responseTimeNs;
        this.caCertificates = Collections.unmodifiableList(b.caCertificates);
        this.issuerCertificate = b.issuerCertificate;
        this.statusCode = b.statusCode;
    }
}