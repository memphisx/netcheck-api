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
package com.bompotis.netcheck.service.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public abstract class AbstractHttpChecker {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpChecker.class);

    private static final Set<Integer> STATUS_CODES_WITH_EMPTY_RESPONSES = Set.of(
            HttpURLConnection.HTTP_NOT_FOUND,
            HttpURLConnection.HTTP_BAD_REQUEST,
            HttpURLConnection.HTTP_NO_CONTENT,
            HttpURLConnection.HTTP_UNAVAILABLE,
            HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
            HttpURLConnection.HTTP_VERSION,
            HttpURLConnection.HTTP_INTERNAL_ERROR,
            HttpURLConnection.HTTP_FORBIDDEN,
            422
    );

    private static final Set<Integer> REDIRECT_STATUS_CODES = Set.of(
            HttpURLConnection.HTTP_MOVED_TEMP,
            HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_SEE_OTHER,
            307
    );

    private Boolean checkAssembler(HttpCheckDto.Builder httpCheckDtoBuilder, HttpURLConnection conn, long beginTime) throws IOException {
        var connectedSuccessfully = true;
        var hostname = conn.getURL().getHost();
        try {
            httpCheckDtoBuilder
                    .hostname(hostname)
                    .timeCheckedOn(new Date());
            httpCheckDtoBuilder
                    .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                    .dnsResolved(true);
            var responseCode = conn.getResponseCode();
            if (!STATUS_CODES_WITH_EMPTY_RESPONSES.contains(responseCode)) {
                conn.getInputStream();
            } else {
                LOG.info("{} status code received while checking {}. Skipping data fetch.", responseCode, hostname);
            }
            if (REDIRECT_STATUS_CODES.contains(responseCode)) {
                httpCheckDtoBuilder.redirectUri(conn.getHeaderField("Location"));
            }
            httpCheckDtoBuilder.statusCode(responseCode)
                    .connectionAccepted(true)
                    .responseTimeNs(System.nanoTime() - beginTime)
                    .statusCode(responseCode);
        } catch (SocketTimeoutException e) {
            LOG.error("Socket timeout for {}.", hostname);
            httpCheckDtoBuilder.dnsResolved(true).connectionAccepted(false);
            connectedSuccessfully = false;
        } catch (UnknownHostException e) {
            LOG.error("Unknown Host for {}.", hostname);
            httpCheckDtoBuilder.dnsResolved(false).connectionAccepted(false);
            connectedSuccessfully = false;
        } catch (NoRouteToHostException | ConnectException e) {
            LOG.error("Connection Exception for {}: {}", hostname, e.getMessage());
            httpCheckDtoBuilder.connectionAccepted(false).dnsResolved(true);
            connectedSuccessfully = false;
        }
        return connectedSuccessfully;
    }

    protected HttpsCheckDto checkHttps(String domain) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTPS");
        HttpsURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            var connected = false;
            conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            try {
                connected = checkAssembler(httpCheckDtoBuilder, conn, beginTime);
            } catch (SSLHandshakeException e) {
                LOG.warn("SSL Handshake failed for domain {} probably because of invalid cert: {}", domain, e.getMessage());
                LOG.warn("Retrying with more permissive Certificate Handling to get additional details");
                conn.disconnect();
                beginTime = System.nanoTime();
                conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new X509TrustEverythingManager[]{new X509TrustEverythingManager()}, new SecureRandom());
                conn.setHostnameVerifier(new AllHostnamesValidVerifier());
                conn.setSSLSocketFactory(sc.getSocketFactory());
                connected = checkAssembler(httpCheckDtoBuilder, conn, beginTime);
            }
            if (connected) {
                var serverCerts = conn.getServerCertificates();
                Arrays.stream(serverCerts)
                        .filter(cert -> cert instanceof X509Certificate)
                        .map(cert -> new CertificateDetailsDto((X509Certificate) cert))
                        .forEach(httpsCheckDtoBuilder::certificate);
            }
        } finally {
            Optional.ofNullable(conn)
                    .ifPresent(HttpURLConnection::disconnect);
        }
        return httpsCheckDtoBuilder.httpCheckDto(httpCheckDtoBuilder.build()).build();
    }


    protected HttpCheckDto checkHttp(String domain) throws IOException {
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTP");
        HttpURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpURLConnection) getHttpDomainUri(domain).openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            checkAssembler(httpCheckDtoBuilder, conn, beginTime);
        } finally {
            Optional.ofNullable(conn)
                    .ifPresent(HttpURLConnection::disconnect);
        }
        return httpCheckDtoBuilder.build();
    }



    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }


    private URL getHttpDomainUri(String domain) throws MalformedURLException {
        return new URL("http://" + domain);
    }

    protected static class X509TrustEverythingManager implements X509TrustManager {
        private static final Logger TRUSTMANAGERLOG = LoggerFactory.getLogger(X509TrustEverythingManager.class);
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            TRUSTMANAGERLOG.info("Skipping check if client is trusted");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            TRUSTMANAGERLOG.info("Skipping check if server is trusted");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    protected static class AllHostnamesValidVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
