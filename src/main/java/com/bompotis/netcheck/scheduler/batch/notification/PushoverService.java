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

    private final PushoverRestClient pushoverRestClient;

    private final PushoverConfig pushoverConfig;

    private static final Logger log = LoggerFactory.getLogger(PushoverService.class);

    @Autowired
    public PushoverService(PushoverConfig pushoverConfig) {
        this.pushoverRestClient = new PushoverRestClient();
        this.pushoverConfig = pushoverConfig;
    }

    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(pushoverConfig.getEnabled()).orElse(false);
    }

    @Override
    public void notify(NotificationDto notification) throws PushoverException {
        Set<String> protocols = pushoverConfig.getNotifyOnlyFor().isEmpty() ? Set.of("HTTP","HTTPS","CERTIFICATE") : Set.copyOf(pushoverConfig.getNotifyOnlyFor());
        if (isEnabled() && protocols.contains(notification.getType().name())) {
            var status = pushoverRestClient.pushMessage(PushoverMessage
                    .builderWithApiToken(pushoverConfig.getApiToken())
                    .setUserId(pushoverConfig.getUserIdToken())
                    .setMessage(notification.getMessage())
                    .build()
            );
            log.info("Pushover notification: Request id {} - Status {}.",status.getRequestId(),status.getStatus());
        } else {
            var message = isEnabled() ?
                    "Notifications for type " + notification.getType().name() + "are disabled. Skipping!" :
                    "Pushover notifications disabled. Skipping!";
            log.info(message);
        }
    }
}
