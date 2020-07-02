package com.bompotis.netcheck.scheduler.batch.notification;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
public class NotificationDto {
    private final String message;

    public NotificationDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
