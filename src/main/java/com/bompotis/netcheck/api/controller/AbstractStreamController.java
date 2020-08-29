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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kyriakos Bompotis on 26/8/20.
 */
public abstract class AbstractStreamController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractStreamController.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    protected SseEmitter addSseEmitter() {
        return addSseEmitter(new SseEmitter(0L));
    }

    protected SseEmitter addSseEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);

        emitter.onCompletion(() -> {
            logger.info("Emitter completed: {}", emitter);
            this.emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            logger.info("Emitter timed out: {}", emitter);
            emitter.complete();
            this.emitters.remove(emitter);
        });

        return emitter;
    }

    protected void sendToSseEmitter(SseEmitter.SseEventBuilder builder) {
        send(emitter -> emitter.send(builder));
    }

    protected void send(SseEmitterConsumer<SseEmitter> consumer) {
        List<SseEmitter> failedEmitters = new ArrayList<>();

        this.emitters.forEach(emitter -> {
            try {
                consumer.accept(emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
                failedEmitters.add(emitter);
                logger.error("Emitter failed: {}", emitter, e);
            }
        });

        this.emitters.removeAll(failedEmitters);
    }

    @FunctionalInterface
    private interface SseEmitterConsumer<T> {
        void accept(T t) throws IOException;
    }
}
