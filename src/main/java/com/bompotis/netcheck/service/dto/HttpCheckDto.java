package com.bompotis.netcheck.service.dto;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 15/6/20.
 */
public class HttpCheckDto {
    private final String id;
    private final Long responseTimeNs;
    private final String hostname;
    private final Integer statusCode;
    private final Boolean dnsResolved;
    private final String ipAddress;
    private final Date timeCheckedOn;
    private final String protocol;

    public Integer getStatusCode() {
        return statusCode;
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

    public String getId() {
        return id;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public String getProtocol() {
        return protocol;
    }

    public static class Builder {
        private Long responseTimeNs;
        private String hostname;
        private Integer statusCode;
        private Boolean dnsResolved;
        private String ipAddress;
        private String id;
        private Date timeCheckedOn;
        private String protocol;

        public Builder dnsResolved(Boolean dnsResolved) {
            this.dnsResolved = dnsResolved;
            return this;
        }

        public Builder timeCheckedOn(Date timeCheckedOn) {
            this.timeCheckedOn = timeCheckedOn;
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

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
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

        public HttpCheckDto build() {
            return new HttpCheckDto(this);
        }
    }

    private HttpCheckDto(Builder b) {
        this.ipAddress = b.ipAddress;
        this.hostname = b.hostname;
        this.dnsResolved = b.dnsResolved;
        this.responseTimeNs = b.responseTimeNs;
        this.statusCode = b.statusCode;
        this.timeCheckedOn = b.timeCheckedOn;
        this.protocol = b.protocol;
        this.id = b.id;
    }
}
