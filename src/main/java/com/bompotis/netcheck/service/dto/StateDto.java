package com.bompotis.netcheck.service.dto;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Kyriakos Bompotis on 24/6/20.
 */
public class StateDto {
    private final String id;
    private final Integer statusCode;
    private final Date timeCheckedOn;
    private final Boolean dnsResolves;
    private final Boolean connectionAccepted;
    private final String hostname;
    private final String protocol;
    private final String redirectUri;
    private final StateDto previousState;
    private final String changeType;
    private final Duration duration;

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getHostname() {
        return hostname;
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

    public String getRedirectUri() {
        return redirectUri;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public Boolean isUp() {
        AtomicBoolean result = new AtomicBoolean(false);
        Optional.ofNullable(statusCode).ifPresent(
                (code) -> {
                    if (code < 400 ) {
                        result.set(true);
                    }
                }
        );
        return result.get();
    }

    public String getReason() {
        if (changeType.equals("FIRST_CHECK")) {
            return "First Check";
        }
        if (changeType.equals("NO_CHANGE")) {
            return "WHY ARE WE HERE?";
        }
        if (changeType.contains(protocol + "_") && Optional.ofNullable(previousState).isPresent()) {
            if (!dnsResolves && previousState.getDnsResolves()) {
                return "DOWN. Domain name cannot be resolved";
            }
            if (!connectionAccepted && previousState.getConnectionAccepted()) {
                return "DOWN. Connection refused";
            }
            if (!isUp() && previousState.isUp()) {
                return "DOWN. HTTP RESPONSE STATUS CODE " + statusCode;
            }
            if (changeType.contains("CERT_")) {
                return "Certificates Changed";
            }
            if (Optional.ofNullable(previousState.redirectUri).isPresent()) {
                if (!statusCode.equals(previousState.statusCode) && !redirectUri.equals(previousState.redirectUri)) {
                    return "UP. Redirects to" + redirectUri;
                }
                if (statusCode.equals(previousState.statusCode) && !redirectUri.equals(previousState.redirectUri)) {
                    return "Redirection changed to " + redirectUri;
                }
            }
            if (isUp() && !previousState.isUp()) {
                return "UP with response status code " + statusCode;
            }
        }
        return "We shouldn't be here";
    }

    public Duration getDuration() {
        return duration;
    }

    public StateDto getPreviousState() {
        return previousState;
    }

    public String getChangeType() {
        return changeType;
    }

    public Boolean getConnectionAccepted() {
        return connectionAccepted;
    }

    public static class Builder {
        private String id;
        private Integer statusCode;
        private Date timeCheckedOn;
        private Boolean dnsResolves;
        private Boolean connectionAccepted;
        private String hostname;
        private String changeType;
        private String protocol;
        private String redirectUri;
        private StateDto previousState;
        private Duration duration;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder timeCheckedOn(Date timeCheckedOn) {
            this.timeCheckedOn = timeCheckedOn;
            return this;
        }

        public Builder dnsResolves(Boolean dnsResolves) {
            this.dnsResolves = dnsResolves;
            return this;
        }

        public Builder connectionAccepted(Boolean connectionAccepted) {
            this.connectionAccepted = connectionAccepted;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder changeType(String changeType) {
            this.changeType = changeType;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder previousState(StateDto previousState) {
            this.previousState = previousState;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public StateDto build() {
            return new StateDto(this);
        }
    }

    private StateDto(Builder b) {
        this.id = b.id;
        this.statusCode = b.statusCode;
        this.timeCheckedOn = b.timeCheckedOn;
        this.dnsResolves = b.dnsResolves;
        this.connectionAccepted = b.connectionAccepted;
        this.hostname = b.hostname;
        this.protocol = b.protocol;
        this.redirectUri = b.redirectUri;
        this.previousState = b.previousState;
        this.duration = b.duration;
        this.changeType = b.changeType;
    }
}
