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
package com.bompotis.netcheck.scheduler.batch.writer;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationDto;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationService;
import com.bompotis.netcheck.scheduler.batch.processor.DomainMetricProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
@Component
public class NotificationItemWriter extends AbstractNotificationWriter implements ItemWriter<DomainCheckEntity> {

    private static final Logger log = LoggerFactory.getLogger(DomainMetricProcessor.class);

    private final List<NotificationService> notificationServices;

    @Autowired
    public NotificationItemWriter(List<NotificationService> notificationServices) {
        var enabledNotificationServices = new ArrayList<NotificationService>();
        for (var notificationService : notificationServices) {
            log.info("Notification provider: {} - Enabled: {}", notificationService.getClass(), notificationService.isEnabled());
            if (notificationService.isEnabled()) {
                enabledNotificationServices.add(notificationService);
            }
        }
        this.notificationServices = Collections.unmodifiableList(enabledNotificationServices);
    }

    public boolean isEnabled() {
        return !notificationServices.isEmpty();
    }

    private void send(List<NotificationDto> notifications) {
        notifications.forEach(this::send);
    }

    private void send(NotificationDto notification){
        for (NotificationService service : notificationServices) {
            try {
                service.notify(notification);
            } catch (Exception e) {
                log.error("Failure to send notification for service {}. Failed notification message: {}", service.getClass(), notification.getMessage(), e);
            }
        }
    }

    @Override
    public void write(@NonNull Chunk<? extends DomainCheckEntity> chunk) {
        var notifications = generateNotifications(chunk);
        send(notifications);
    }
}
