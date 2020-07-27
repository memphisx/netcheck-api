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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainCheckDto {
    private final HttpCheckDto httpCheckDto;
    private final HttpsCheckDto httpsCheckDto;
    private final Boolean monitored;
    private final String domain;
    private final String id;

    public HttpCheckDto getHttpCheckDto() {
        return httpCheckDto;
    }

    public HttpsCheckDto getHttpsCheckDto() {
        return httpsCheckDto;
    }

    public String getDomain() {
        return domain;
    }

    public String getId() {
        return id;
    }

    public Boolean getMonitored() {
        return monitored;
    }

    public static class Builder extends AbstractHttpChecker{
        private HttpCheckDto httpCheckDto;
        private HttpsCheckDto httpsCheckDto;
        private final String domain;
        private Boolean monitored;
        private String id;

        public Builder(String domain) {
            this.domain = domain;
        }

        public Builder monitored(Boolean monitored) {
            this.monitored = monitored;
            return this;
        }

        public Builder httpCheck(HttpCheckDto httpCheckDto) {
            this.httpCheckDto = httpCheckDto;
            return this;
        }

        public Builder httpsCheckDto(HttpsCheckDto httpsCheckDto) {
            this.httpsCheckDto = httpsCheckDto;
            return this;
        }

        public Builder withCurrentHttpCheck() throws IOException {
            this.httpCheckDto = checkHttp(this.domain);
            return this;
        }

        public Builder withCurrentHttpsCheck() throws IOException, KeyManagementException, NoSuchAlgorithmException {
            this.httpsCheckDto = checkHttps(this.domain);
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public DomainCheckDto build() {
            return new DomainCheckDto(this);
        }
    }

    private DomainCheckDto(Builder b) {
        this.httpCheckDto = b.httpCheckDto;
        this.domain = b.domain;
        this.httpsCheckDto = b.httpsCheckDto;
        this.monitored = b.monitored;
        this.id = b.id;
    }
}