package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoryEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import org.springframework.data.domain.PageRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
public class DomainService {

    private final String domain;

    private final DomainRepository domainRepository;

    private final DomainHistoryEntryRepository domainHistoryEntryRepository;

    public DomainService(String domain, DomainRepository domainRepository, DomainHistoryEntryRepository domainHistoryEntryRepository) throws MalformedURLException {
        this.domain = domain;
        this.domainRepository = domainRepository;
        this.domainHistoryEntryRepository = domainHistoryEntryRepository;
    }

    public List<DomainHistoricEntryEntity> getDomainHistory() {
        var page = PageRequest.of(0,10);
        return domainHistoryEntryRepository.findAllByDomainEntityDomain(this.domain, page).getContent();
    }

    private URL getHttpsDomainUri() throws MalformedURLException {
        return new URL("https://" + domain);
    }

    public DomainStatus checkCerts(Boolean store) throws IOException {
        var certificates = new ArrayList<CertDetails>();
        try {
            var conn = (HttpsURLConnection) getHttpsDomainUri().openConnection();
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
            var result = new DomainStatus(hostname, ipAddress, statusCode, certificates, domainRepository, domainHistoryEntryRepository);
            if(Optional.ofNullable(store).isPresent() && store) {
                result.storeResult();
            }
            return result;
        } catch (UnknownHostException e) {
            return new DomainStatus(domainRepository, domainHistoryEntryRepository);
        }
    }

    static class DomainStatus {
        private final List<CertDetails> caCertificates = new ArrayList<>();
        private CertDetails issuerCertificate = null;
        private Integer statusCode;
        private Boolean dnsResolved = true;
        private String ipAddress;
        private String hostname;
        private final DomainRepository domainRepository;
        private final DomainHistoryEntryRepository domainHistoryEntryRepository;

        DomainStatus(DomainRepository domainRepository, DomainHistoryEntryRepository domainHistoryEntryRepository) {
            this.dnsResolved = false;
            this.domainRepository = domainRepository;
            this.domainHistoryEntryRepository = domainHistoryEntryRepository;
        }

        DomainStatus(String hostname, String ipAddress, Integer statusCode, List<CertDetails> certificates, DomainRepository domainRepository, DomainHistoryEntryRepository domainHistoryEntryRepository) {
            this.ipAddress = ipAddress;
            this.domainRepository = domainRepository;
            this.domainHistoryEntryRepository = domainHistoryEntryRepository;
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

        public void storeResult() {
            var domainEntity = new DomainEntity();
            var domainEntityOptional = domainRepository.findById(hostname);
            if (domainEntityOptional.isPresent()) {
                domainEntity = domainEntityOptional.get();
            } else {
                domainEntity.setDomain(hostname);
            }
            var domainHistoryEntity = new DomainHistoricEntryEntity();
            domainHistoryEntity.setCertificateExpiresOn(issuerCertificate.getNotAfter());
            domainHistoryEntity.setCertificateIsValid(issuerCertificate.isValid());
            domainHistoryEntity.setDnsResolves(dnsResolved);
            domainHistoryEntity.setStatusCode(statusCode);
            domainHistoryEntity.setTimeCheckedOn(new Date());
            domainHistoryEntity.setDomainEntity(domainEntity);
            domainHistoryEntryRepository.save(domainHistoryEntity);
        }
    }

}
