package com.bompotis.netcheck.service.dto;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.*;
import java.security.cert.X509Certificate;
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
            422
    );

    private static final Set<Integer> REDIRECT_STATUS_CODES = Set.of(
            HttpURLConnection.HTTP_MOVED_TEMP,
            HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_SEE_OTHER
    );

    private void checkAssembler(HttpCheckDto.Builder httpCheckDtoBuilder, HttpURLConnection conn, long beginTime) throws IOException {
        var hostname = conn.getURL().getHost();
        httpCheckDtoBuilder
                .hostname(hostname)
                .timeCheckedOn(new Date())
                .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                .dnsResolved(true);
        var responseCode = conn.getResponseCode();
        if (!STATUS_CODES_WITH_EMPTY_RESPONSES.contains(responseCode)) {
            conn.getInputStream();
        }
        if (REDIRECT_STATUS_CODES.contains(responseCode)) {
            httpCheckDtoBuilder.redirectUri(conn.getHeaderField("Location"));
        }
        httpCheckDtoBuilder.statusCode(conn.getResponseCode())
                .responseTimeNs(System.nanoTime() - beginTime);
        httpCheckDtoBuilder.statusCode(responseCode);
    }

    protected HttpsCheckDto checkHttps(String domain) throws IOException {
        var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTPS");
        HttpsURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            checkAssembler(httpCheckDtoBuilder, conn, beginTime);
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    httpsCheckDtoBuilder
                            .certificate(new CertificateDetailsDto((X509Certificate ) cert));
                }
            }
        } catch (UnknownHostException e) {
            httpCheckDtoBuilder.dnsResolved(false);
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
        } catch (UnknownHostException e) {
            httpCheckDtoBuilder.dnsResolved(false);
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
