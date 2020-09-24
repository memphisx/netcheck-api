/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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

    public static class Builder implements DtoBuilder<HttpsCheckDto> {
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
