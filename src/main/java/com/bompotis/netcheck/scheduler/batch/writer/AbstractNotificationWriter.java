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
package com.bompotis.netcheck.scheduler.batch.writer;

import com.bompotis.netcheck.data.entity.CertificateEntity;
import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by Kyriakos Bompotis on 25/8/20.
 */
public abstract class AbstractNotificationWriter {

    protected List<NotificationDto> generateNotifications(List<? extends DomainCheckEntity> list) {
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
            if (check.isHttpsCheckChange()) {
                var previousHttpCheck = previousCheckMap.get("HTTPS");
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
                (certs) -> certs.stream().filter(cert -> cert.getBasicConstraints() < 0).findFirst().ifPresent(certificate::set),
                () -> certificate.set(null)
        );
        return certificate.get();

    }
}
