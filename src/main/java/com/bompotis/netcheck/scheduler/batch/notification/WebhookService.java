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

import com.bompotis.netcheck.scheduler.batch.notification.config.WebhookConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 10/8/20.
 */
@Service
public class WebhookService implements NotificationService {

    private final WebhookConfig webhookConfig;

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    public WebhookService(WebhookConfig webhookConfig) {
        this.webhookConfig = webhookConfig;
    }

    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(webhookConfig.getEnabled()).orElse(false);
    }

    @Override
    public String name() {
        return "Webhook";
    }

    @Override
    public void notify(NotificationDto notification) {
        Set<String> protocols = webhookConfig.getNotifyOnlyFor().isEmpty() ? Set.of("HTTP","HTTPS","CERTIFICATE") : Set.copyOf(webhookConfig.getNotifyOnlyFor());
        if (protocols.contains(notification.getType().name())) {
            var url = Optional.ofNullable(webhookConfig.getPort()).isPresent()
                    ? webhookConfig.getBaseUrl() + ":" + webhookConfig.getPort() + webhookConfig.getEndpoint()
                    : webhookConfig.getBaseUrl() + webhookConfig.getEndpoint();
            log.info("Preparing client for domain {}", url);
            var client = WebClient.create(url);
            log.info("Sending notification to {}", webhookConfig.getEndpoint());
            var response = Objects.requireNonNull(client.post()
                    .body(BodyInserters.fromValue(notification))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .exchange()
                    .block(Duration.ofSeconds(30)))
                    .rawStatusCode();
            log.info("Webhook response code: {}", response);
        } else {
            log.info("Webhook notifications for type {} are disabled. Skipping!", notification.getType().name());
        }
    }
}
