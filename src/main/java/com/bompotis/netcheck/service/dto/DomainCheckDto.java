package com.bompotis.netcheck.service.dto;

import java.io.IOException;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainCheckDto {
    private final HttpCheckDto httpCheckDto;
    private final HttpsCheckDto httpsCheckDto;
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

    public static class Builder extends AbstractHttpChecker{
        private HttpCheckDto httpCheckDto;
        private HttpsCheckDto httpsCheckDto;
        private final String domain;
        private String id;

        public Builder(String domain) {
            this.domain = domain;
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

        public Builder withCurrentHttpsCheck() throws IOException {
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
        this.id = b.id;
    }
}