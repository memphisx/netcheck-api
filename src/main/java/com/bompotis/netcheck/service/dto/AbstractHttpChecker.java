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
            HttpURLConnection.HTTP_BAD_GATEWAY,
            422,
            523
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
            httpCheckDtoBuilder.errorMessage(e.getMessage()).dnsResolved(true).connectionAccepted(false);
            connectedSuccessfully = false;
        } catch (UnknownHostException e) {
            LOG.error("Unknown Host for {}.", hostname);
            httpCheckDtoBuilder.errorMessage(e.getMessage()).dnsResolved(false).connectionAccepted(false);
            connectedSuccessfully = false;
        } catch (NoRouteToHostException | ConnectException e) {
            LOG.error("Connection Exception for {}: {}", hostname, e.getMessage());
            httpCheckDtoBuilder.errorMessage(e.getMessage()).connectionAccepted(false).dnsResolved(true);
            connectedSuccessfully = false;
        }
        return connectedSuccessfully;
    }

    protected HttpsCheckDto checkHttps(DomainCheckConfigDto config, int port) {
        var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTPS");
        HttpsURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            var connected = false;
            conn = (HttpsURLConnection) getHttpsDomainUri(config.getDomain(), config.getEndpoint(), port).openConnection();
            assignHeaders(config, conn);
            try {
                connected = checkAssembler(httpCheckDtoBuilder, conn, beginTime);
            } catch(SSLException e) {
                LOG.warn("SSL Handshake failed for domain {} probably because of invalid cert: {}", config.getDomain(), e.getMessage());
                LOG.warn("Retrying with more permissive Certificate Handling to get additional details");
                conn.disconnect();
                beginTime = System.nanoTime();
                conn = (HttpsURLConnection) getHttpsDomainUri(config.getDomain(),config.getEndpoint(), port).openConnection();
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
        } catch(Exception e) {
            LOG.error("Error on HTTP check",e);
            httpCheckDtoBuilder.errorMessage(e.getMessage());
        } finally {
            Optional.ofNullable(conn)
                    .ifPresent(HttpURLConnection::disconnect);
        }
        return httpsCheckDtoBuilder.httpCheckDto(httpCheckDtoBuilder.build()).build();
    }


    protected HttpCheckDto checkHttp(DomainCheckConfigDto config, int port) {
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTP");
        HttpURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpURLConnection) getHttpDomainUri(config.getDomain(), config.getEndpoint(), port).openConnection();
            assignHeaders(config, conn);
            checkAssembler(httpCheckDtoBuilder, conn, beginTime);
        } catch(Exception e) {
            LOG.error("Error on HTTP check",e);
          httpCheckDtoBuilder.errorMessage(e.getMessage());
        } finally {
            Optional.ofNullable(conn).ifPresent(HttpURLConnection::disconnect);
        }
        return httpCheckDtoBuilder.build();
    }

    private void assignHeaders(DomainCheckConfigDto config, HttpURLConnection conn) {
        if (Optional.ofNullable(config.getHeaders()).isPresent() && !config.getHeaders().isEmpty()) {
            for (var key : config.getHeaders().keySet()) {
                conn.setRequestProperty(key,config.getHeaders().get(key));
            }
        }
        conn.setConnectTimeout(config.getTimeoutMs());
        conn.setReadTimeout(config.getTimeoutMs());
    }


    private URL getHttpsDomainUri(String domain, String endpoint, int port) throws MalformedURLException {
        var url = new URL("https://" + domain + Optional.ofNullable(endpoint).orElse(""));
        return new URL(url.getProtocol(), url.getHost(), port, url.getFile());
    }


    private URL getHttpDomainUri(String domain, String endpoint, int port) throws MalformedURLException {
        var url = new URL("http://" + domain + Optional.ofNullable(endpoint).orElse(""));
        return new URL(url.getProtocol(), url.getHost(), port, url.getFile());
    }

    protected static class X509TrustEverythingManager implements X509TrustManager {
        private static final Logger TRUST_MANAGER_LOG = LoggerFactory.getLogger(X509TrustEverythingManager.class);
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            TRUST_MANAGER_LOG.info("Skipping check if client is trusted");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            TRUST_MANAGER_LOG.info("Skipping check if server is trusted");
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
