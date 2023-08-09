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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Created by Kyriakos Bompotis on 25/8/20.
 */
@Service
public class NotificationEventService implements NotificationService {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public NotificationEventService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String name() {
        return "NotificationEvent";
    }

    @Override
    public void notify(NotificationDto notification) {
        eventPublisher.publishEvent(new NotificationEventDto(this, notification));
    }
}
