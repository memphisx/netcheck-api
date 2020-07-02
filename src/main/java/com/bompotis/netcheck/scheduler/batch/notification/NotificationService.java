package com.bompotis.netcheck.scheduler.batch.notification;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
public interface NotificationService {
    boolean isEnabled();
    void notify(NotificationDto notification) throws Exception;
}
