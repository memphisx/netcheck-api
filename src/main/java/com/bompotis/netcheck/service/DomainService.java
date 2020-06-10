package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
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

    private final DomainHistoricEntryRepository domainHistoricEntryRepository;

    public DomainService(String domain, DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository) throws MalformedURLException {
        this.domain = domain;
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
    }

    public List<DomainHistoricEntryEntity> getDomainHistory() {
        var page = PageRequest.of(0,10);
        return domainHistoricEntryRepository.findAllByDomainEntityDomain(this.domain, page).getContent();
    }

    private URL getHttpsDomainUri() throws MalformedURLException {
        return new URL("https://" + domain);
    }

    public DomainStatus checkCerts() throws IOException {
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
            return new DomainStatus(hostname, ipAddress, statusCode, certificates, domainRepository, domainHistoricEntryRepository);
        } catch (UnknownHostException e) {
            return new DomainStatus(domainRepository, domainHistoricEntryRepository);
        }
    }

}
