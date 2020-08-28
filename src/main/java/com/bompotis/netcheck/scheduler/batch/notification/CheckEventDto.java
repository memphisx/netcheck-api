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

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.google.gson.Gson;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Created by Kyriakos Bompotis on 25/8/20.
 */
public class CheckEventDto extends ApplicationEvent implements EventDto {

    private final SseEmitter.SseEventBuilder event;

    public CheckEventDto(Object source, DomainCheckEntity domainCheckEntity) {
        super(source);
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("domain",domainCheckEntity.getDomain());
        messageMap.put("timeCheckedOn", domainCheckEntity.getTimeCheckedOn());
        messageMap.put("changeType", domainCheckEntity.getChangeType());
        event = SseEmitter.event()
                .data(new Gson().toJson(messageMap), APPLICATION_JSON)
                .reconnectTime(1000L)
                .name("DomainCheck_"+domainCheckEntity.getDomain());
    }

    public SseEmitter.SseEventBuilder getEvent() {
        return event;
    }
}
