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
import java.util.concurrent.atomic.AtomicReference;
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
            var previousCheckMap = mapChecks(check.getPreviousProtocolCheckEntities());

            if (check.isHttpCheckChange()) {
                var httpCheck = checkMap.get("HTTP");
                var previousHttpCheck = previousCheckMap.get("HTTP");
                notifications.add(new NotificationDto.Builder()
                        .hostname(httpCheck.getHostname())
                        .currentState(httpCheck)
                        .previousState(previousHttpCheck)
                        .connectionAccepted(httpCheck.isConnectionAccepted())
                        .dnsResolves(httpCheck.getDnsResolves())
                        .ipAddress(check.getHttpIpAddress())
                        .redirectUri(httpCheck.getRedirectUri())
                        .responseTimeNs(check.getHttpResponseTimeNs())
                        .statusCode(httpCheck.getStatusCode())
                        .timeCheckedOn(check.getTimeCheckedOn())
                        .type(NotificationDto.Type.HTTP)
                        .build());
            }
            var httpCheck = checkMap.get("HTTPS");
            var previousHttpCheck = previousCheckMap.get("HTTP");
            if (check.isHttpsCheckChange()) {
                notifications.add(new NotificationDto.Builder()
                        .hostname(httpCheck.getHostname())
                        .currentState(httpCheck)
                        .previousState(previousHttpCheck)
                        .connectionAccepted(httpCheck.isConnectionAccepted())
                        .dnsResolves(httpCheck.getDnsResolves())
                        .ipAddress(check.getHttpIpAddress())
                        .redirectUri(httpCheck.getRedirectUri())
                        .responseTimeNs(check.getHttpResponseTimeNs())
                        .statusCode(httpCheck.getStatusCode())
                        .timeCheckedOn(check.getTimeCheckedOn())
                        .type(NotificationDto.Type.HTTPS)
                        .build());
            }
            var issuerCertificate = getIssuerCertificate(check.getCertificateEntities());
            var previousIssuerCertificate = getIssuerCertificate(check.getPreviousCertificateEntities());
            if (check.isCertificatesChange() && Optional.ofNullable(issuerCertificate).isPresent()) {
                notifications.add(new NotificationDto.Builder()
                        .hostname(httpCheck.getHostname())
                        .currentState(issuerCertificate)
                        .previousState(previousIssuerCertificate)
                        .connectionAccepted(httpCheck.isConnectionAccepted())
                        .issuerCertificateExpirationDate(issuerCertificate.getNotAfter())
                        .issuerCertificateHasExpired(issuerCertificate.getExpired())
                        .issuerCertificateIsValid(issuerCertificate.isValid())
                        .timeCheckedOn(check.getTimeCheckedOn())
                        .type(NotificationDto.Type.CERTIFICATE)
                        .build());
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
        AtomicReference<CertificateEntity> certificate = new AtomicReference<>();
        Optional.ofNullable(certificates).ifPresentOrElse(
                (certs) -> certs.stream().filter(cert -> cert.getBasicConstraints() > 0).findFirst().ifPresent(certificate::set),
                () -> certificate.set(null)
        );
        return certificate.get();

    }
}
