package com.bompotis.netcheck.scheduler.batch.notification;

import net.pushover.client.PushoverException;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
@Service
public class PushoverService implements NotificationService {

    private final PushoverRestClient pushoverRestClient;

    private static final Logger log = LoggerFactory.getLogger(PushoverService.class);

    @Value("${settings.notifications.pushover.enabled:false}")
    private Boolean enabled;

    @Value("${settings.notifications.pushover.userIdToken:}")
    private String userIdToken;

    @Value("${settings.notifications.pushover.apiToken:}")
    private String apiToken;

    @Autowired
    public PushoverService() {
        this.pushoverRestClient = new PushoverRestClient();
    }

    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(enabled).orElse(false);
    }

    @Override
    public void notify(NotificationDto notification) throws PushoverException {
        if (isEnabled()) {
            var status = pushoverRestClient.pushMessage(PushoverMessage
                    .builderWithApiToken(apiToken)
                    .setUserId(userIdToken)
                    .setMessage(notification.getMessage())
                    .build()
            );
            log.info("Pushover notification: Request id {} - Status {}.",status.getRequestId(),status.getStatus());
        } else {
            log.info("Pushover notifications disabled. Skipping");
        }
    }
}
