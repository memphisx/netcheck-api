package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
@Service
public class DomainService {

    private final DomainRepository domainRepository;

    private final DomainHistoricEntryRepository domainHistoricEntryRepository;

    @Autowired
    public DomainService(DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository) {
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
    }

    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }

    public DomainStatus buildAndCheck(String domain) throws IOException {
        var certificates = new ArrayList<CertificateDetails>();
        try {
            var conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            var hostname = conn.getURL().getHost();
            var ipAddress = InetAddress.getByName(hostname).getHostAddress();
            var statusCode = conn.getResponseCode();
            conn.connect();
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    X509Certificate x = (X509Certificate ) cert;
                    certificates.add(new CertificateDetails(x));
                }
            }
            return new DomainStatus(hostname, ipAddress, statusCode, certificates, domainRepository, domainHistoricEntryRepository);
        } catch (UnknownHostException e) {
            return new DomainStatus(domainRepository, domainHistoricEntryRepository);
        }
    }

    public List<DomainHistoricEntryEntity> getDomainHistory(String domain) {
        var page = PageRequest.of(0,10);
        return domainHistoricEntryRepository.findAllByDomainEntityDomain(domain, page).getContent();
    }

    public void scheduleDomainToCheck(String domain) {
        var domainEntity = new DomainEntity();
        domainEntity.setDomain(domain);
        domainRepository.save(domainEntity);
    }

}
