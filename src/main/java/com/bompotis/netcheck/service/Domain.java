package com.bompotis.netcheck.service;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
public class Domain {

    private final URL domainURI;

    public Domain(String domain) throws MalformedURLException {
        this.domainURI = new URL(domain);
    }

    public DomainStatus checkCerts() throws IOException {
        var certificates = new ArrayList<CertDetails>();
        try {
            var conn = (HttpsURLConnection) domainURI.openConnection();
            var hostname = conn.getURL().getHost();
            var ipAddress = InetAddress.getByName(hostname).getHostAddress();
            var statusCode = conn.getResponseCode();
            conn.connect();
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    X509Certificate x = (X509Certificate ) cert;
                    certificates.add(new CertDetails(x));
                }
            }
            return new DomainStatus(hostname, ipAddress, statusCode, certificates);
        } catch (UnknownHostException e) {
            return new DomainStatus();
        }
    }

    class DomainStatus {
        private final List<CertDetails> caCertificates = new ArrayList<>();
        private CertDetails issuerCertificate = null;
        private Integer statusCode;
        private Boolean dnsResolved = true;
        private String ipAddress;
        private String hostname;

        DomainStatus() {
            this.dnsResolved = false;
        }

        DomainStatus(String hostname, String ipAddress, Integer statusCode, List<CertDetails> certificates) {
            this.ipAddress = ipAddress;
            this.hostname = hostname;
            for (var cert: certificates) {
                if (cert.getBasicConstraints() < 0) {
                    this.issuerCertificate = cert;
                } else {
                    this.caCertificates.add(cert);
                }
            }
            this.statusCode = statusCode;
        }

        public List<CertDetails> getCaCertificates() {
            return caCertificates;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public CertDetails getIssuerCertificate() {
            return issuerCertificate;
        }

        public Boolean getDnsResolved() {
            return dnsResolved;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getHostname() {
            return hostname;
        }
    }

}
