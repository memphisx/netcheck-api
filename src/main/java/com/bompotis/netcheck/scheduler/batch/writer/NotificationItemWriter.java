package com.bompotis.netcheck.scheduler.batch.writer;

import com.bompotis.netcheck.data.entity.CertificateEntity;
import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationDto;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationService;
import com.bompotis.netcheck.scheduler.batch.processor.DomainMetricProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
@Component
public class NotificationItemWriter implements ItemWriter<DomainCheckEntity> {

    private static final Logger log = LoggerFactory.getLogger(DomainMetricProcessor.class);

    private final List<NotificationService> notificationServices;

    @Autowired
    public NotificationItemWriter(List<NotificationService> notificationServices) {
        var enabledNotificationServices = new ArrayList<NotificationService>();
        for (var notificationService : notificationServices) {
            if (notificationService.isEnabled()) {
                enabledNotificationServices.add(notificationService);
            }
        }
        this.notificationServices = Collections.unmodifiableList(enabledNotificationServices);
    }

    public boolean isEnabled() {
        return !notificationServices.isEmpty();
    }

    @Override
    public void write(List<? extends DomainCheckEntity> list) {
        var notifications = generateNotifications(list);
        send(notifications);
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

    private List<NotificationDto> generateNotifications(List<? extends DomainCheckEntity> list) {
        var notifications = new ArrayList<NotificationDto>();
        for (var check : list) {
            if (!check.isCertificatesChange() && !check.isHttpCheckChange() && !check.isHttpsCheckChange()) {
                continue;
            }
            var checkMap = mapChecks(check.getProtocolCheckEntities());

            if (check.isHttpCheckChange()) {
                var httpCheck = checkMap.get("HTTP");
                boolean isUp = Optional.ofNullable(httpCheck.getStatusCode()).orElse(1000) < 400;
                var message = String.format(
                        "%s State for %s has changed to %s with status code %s",
                        httpCheck.getProtocol().name(),
                        httpCheck.getHostname(),
                        isUp ? "UP" : "DOWN",
                        httpCheck.getStatusCode()
                );
                notifications.add(new NotificationDto(message));
            }
            var httpCheck = checkMap.get("HTTPS");
            if (check.isHttpsCheckChange()) {
                boolean isUp = Optional.ofNullable(httpCheck.getStatusCode()).orElse(1000) < 400;
                var message = String.format(
                        "%s State for %s has changed to %s with status code %s",
                        httpCheck.getProtocol().name(),
                        httpCheck.getHostname(),
                        isUp ? "UP" : "DOWN",
                        httpCheck.getStatusCode()
                );
                notifications.add(new NotificationDto(message));
            }
            var issuerCertificate = getIssuerCertificate(check.getCertificateEntities());
            if (check.isCertificatesChange() && Optional.ofNullable(issuerCertificate).isPresent()) {
                if (issuerCertificate.getExpired()) {
                    var message = String.format(
                            "Certificates for %s has expired. New Expiration date: %s",
                            httpCheck.getHostname(),
                            issuerCertificate.getNotAfter()
                    );
                    notifications.add(new NotificationDto(message));
                } else if (!issuerCertificate.isValid()) {
                    var message = String.format(
                            "Certificates for %s have changed. New certificate is invalid. Expiration Date: %s",
                            httpCheck.getHostname(),
                            issuerCertificate.getNotAfter().toString()
                    );
                    notifications.add(new NotificationDto(message));
                } else {
                    var message = String.format(
                            "Certificates for %s have changed. New Expiration date: %s",
                            httpCheck.getHostname(),
                            issuerCertificate.getNotAfter().toString()
                    );
                    notifications.add(new NotificationDto(message));
                }
            }
        }
        return notifications;
    }

    private Map<String, ProtocolCheckEntity> mapChecks(Set<ProtocolCheckEntity> protocolChecks) {
        return protocolChecks.stream().collect(Collectors
                .toMap(protocolCheck -> protocolCheck.getProtocol().name(),
                        protocolCheck -> protocolCheck,
                        (a, b) -> b,
                        HashMap::new
                )
        );
    }

    private CertificateEntity getIssuerCertificate(Set<CertificateEntity> certificates) {
        CertificateEntity certificate = null;
        for (var cert: certificates) {
            if (cert.getBasicConstraints() > 0) {
                certificate = cert;
                break;
            }
        }
        return certificate;

    }
}
