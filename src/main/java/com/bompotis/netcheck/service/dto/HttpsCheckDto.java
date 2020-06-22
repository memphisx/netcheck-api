package com.bompotis.netcheck.service.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 15/6/20.
 */
public class HttpsCheckDto {
    private final HttpCheckDto httpCheckDto;
    private final List<CertificateDetailsDto> caCertificates;
    private final CertificateDetailsDto issuerCertificate;

    public List<CertificateDetailsDto> getCaCertificates() {
        return caCertificates;
    }

    public CertificateDetailsDto getIssuerCertificate() {
        return issuerCertificate;
    }

    public HttpCheckDto getHttpCheckDto() {
        return httpCheckDto;
    }

    public static class Builder {
        private final List<CertificateDetailsDto> caCertificates = new ArrayList<>();
        private CertificateDetailsDto issuerCertificate;
        private HttpCheckDto httpCheckDto;

        public HttpsCheckDto.Builder httpCheckDto(HttpCheckDto httpCheckDto) {
            this.httpCheckDto = httpCheckDto;
            return this;
        }

        public HttpsCheckDto.Builder certificate(CertificateDetailsDto cert) {
            if (cert.getBasicConstraints() < 0) {
                this.issuerCertificate = cert;
            } else {
                this.caCertificates.add(cert);
            }
            return this;
        }

        public HttpsCheckDto build() {
            return new HttpsCheckDto(this);
        }
    }

    private HttpsCheckDto(HttpsCheckDto.Builder b) {
        this.caCertificates = Collections.unmodifiableList(b.caCertificates);
        this.issuerCertificate = b.issuerCertificate;
        this.httpCheckDto = b.httpCheckDto;
    }
}
