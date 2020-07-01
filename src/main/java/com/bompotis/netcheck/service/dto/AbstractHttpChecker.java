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
            HttpURLConnection.HTTP_SEE_OTHER
    );

    private Boolean checkAssembler(HttpCheckDto.Builder httpCheckDtoBuilder, HttpURLConnection conn, long beginTime) throws IOException {
        var connectedSuccessfully = true;
        try {
            var hostname = conn.getURL().getHost();
            httpCheckDtoBuilder
                    .hostname(hostname)
                    .timeCheckedOn(new Date());
            httpCheckDtoBuilder
                    .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                    .dnsResolved(true);
            var responseCode = conn.getResponseCode();
            if (!STATUS_CODES_WITH_EMPTY_RESPONSES.contains(responseCode)) {
                conn.getInputStream();
            }
            if (REDIRECT_STATUS_CODES.contains(responseCode)) {
                httpCheckDtoBuilder.redirectUri(conn.getHeaderField("Location"));
            }
            httpCheckDtoBuilder.statusCode(responseCode)
                    .connectionAccepted(true)
                    .responseTimeNs(System.nanoTime() - beginTime)
                    .statusCode(responseCode);
        } catch (UnknownHostException e) {
            httpCheckDtoBuilder.dnsResolved(false).connectionAccepted(false);
            connectedSuccessfully = false;
        } catch (ConnectException e) {
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
            try {
                connected = checkAssembler(httpCheckDtoBuilder, conn, beginTime);
            } catch (SSLHandshakeException e) {
                LOG.warn("SSL Handshake failed for domain {} probably because of invalid cert", domain, e);
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
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

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
