package com.bompotis.netcheck.scheduler.batch.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 15/7/20.
 */
@ConfigurationProperties(prefix = "settings.notifications.pushover")
@ConstructorBinding
public class PushoverConfig {

    private final Boolean enabled;

    private final String userIdToken;

    private final String apiToken;

    private final Set<String> notifyOnlyFor;

    public PushoverConfig(Boolean enabled, String userIdToken, String apiToken, Set<String> notifyOnlyFor) {
        this.enabled = enabled;
        this.userIdToken = userIdToken;
        this.apiToken = apiToken;
        var acceptableEntries = Set.of("HTTP","HTTPS","CERTIFICATE");
        if (Optional.ofNullable(notifyOnlyFor).isEmpty() || notifyOnlyFor.isEmpty()) {
            this.notifyOnlyFor = acceptableEntries;
        } else {
            var finalSet = new HashSet<String>();
            for (var entry: notifyOnlyFor) {
                if (acceptableEntries.contains(entry.toUpperCase())) {
                    finalSet.add(entry.toUpperCase());
                }
                else {
                    throw new IllegalArgumentException("No such type of notification:" + entry);
                }
            }
            this.notifyOnlyFor = finalSet;
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getUserIdToken() {
        return userIdToken;
    }

    public String getApiToken() {
        return apiToken;
    }

    public Set<String> getNotifyOnlyFor() {
        return notifyOnlyFor;
    }
}