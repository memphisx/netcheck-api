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
package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.scheduler.batch.notification.CheckEventDto;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationEventDto;
import com.bompotis.netcheck.scheduler.batch.notification.ServerMetricEventDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by Kyriakos Bompotis on 25/8/20.
 */
@RestController
@CrossOrigin(origins = {"${settings.cors.origin}"})
@RequestMapping(value = "/events")
@Tag(name = "Check Events", description = "Server sent events endpoint")
public class StreamController extends AbstractStreamController {
    @GetMapping
    public SseEmitter stream() {
        return addSseEmitter();
    }

    @EventListener
    public void handleCheckEvent(CheckEventDto eventDto) {
        sendToSseEmitter(eventDto.getEvent());
    }

    @EventListener
    public void handleNotificationEvent(NotificationEventDto eventDto) {
        sendToSseEmitter(eventDto.getEvent());
    }

    @EventListener
    public void handleServerMetricsEvent(ServerMetricEventDto eventDto) {
        sendToSseEmitter(eventDto.getEvent());
    }
}
