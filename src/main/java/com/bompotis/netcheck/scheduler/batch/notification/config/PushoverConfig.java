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
package com.bompotis.netcheck.scheduler.batch.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 15/7/20.
 */
@ConfigurationProperties(prefix = "settings.notifications.pushover")
public class PushoverConfig {

    private final boolean enabled;

    private final String userIdToken;

    private final String apiToken;

    private final Set<String> notifyOnlyFor;

    public PushoverConfig(Boolean enabled, String userIdToken, String apiToken, Set<String> notifyOnlyFor) {
        this.enabled = Optional.ofNullable(enabled).orElse(false);
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