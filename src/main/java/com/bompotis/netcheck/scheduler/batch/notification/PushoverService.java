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
package com.bompotis.netcheck.scheduler.batch.notification;

import com.bompotis.netcheck.scheduler.batch.notification.config.PushoverConfig;
import net.pushover.client.PushoverException;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
@Service
public class PushoverService implements NotificationService {

    private final PushoverConfig pushoverConfig;

    private static final Logger log = LoggerFactory.getLogger(PushoverService.class);

    @Autowired
    public PushoverService(PushoverConfig pushoverConfig) {
        this.pushoverConfig = pushoverConfig;
    }

    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(pushoverConfig.getEnabled()).orElse(false);
    }

    @Override
    public void notify(NotificationDto notification) throws PushoverException {
        Set<String> protocols = pushoverConfig.getNotifyOnlyFor().isEmpty() ? Set.of("HTTP","HTTPS","CERTIFICATE") : Set.copyOf(pushoverConfig.getNotifyOnlyFor());
        if (protocols.contains(notification.getType().name())) {
            var status = new PushoverRestClient().pushMessage(PushoverMessage
                    .builderWithApiToken(pushoverConfig.getApiToken())
                    .setUserId(pushoverConfig.getUserIdToken())
                    .setMessage(notification.getMessage())
                    .build()
            );
            log.info("Pushover notification: Request id {} - Status {}.",status.getRequestId(),status.getStatus());
        } else {
            log.info("Pushover notifications for type {} are disabled. Skipping!", notification.getType().name());
        }
    }
}
