package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Kyriakos Bompotis on 11/6/20.
 */
@Relation(collectionRelation = "certificates", itemRelation = "certificate")
public class CertificateModel extends RepresentationModel<CertificateModel> {
    private final String issuedBy;
    private final String issuedFor;
    private final Date notBefore;
    private final Date notAfter;
    private final Boolean isValid;
    private final Boolean expired;

    public CertificateModel(
            @JsonProperty("issuedBy") String issuedBy,
            @JsonProperty("issuedFor") String issuedFor,
            @JsonProperty("notBefore") Date notBefore,
            @JsonProperty("notAfter") Date notAfter,
            @JsonProperty("isValid") Boolean isValid,
            @JsonProperty("expired") Boolean expired) {
        this.issuedBy = issuedBy;
        this.issuedFor = issuedFor;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.isValid = isValid;
        this.expired = expired;
    }

    public Map<String,Object> getIssuedBy() throws InvalidNameException {
        return ldapNameToMap(issuedBy);
    }

    public Map<String,Object> getIssuedFor() throws InvalidNameException {
        return ldapNameToMap(issuedFor);
    }

    private Map<String,Object> ldapNameToMap(String ldapName) throws InvalidNameException {
        return new LdapName(ldapName)
                .getRdns()
                .stream()
                .collect(Collectors.toMap(Rdn::getType, Rdn::getValue, (a, b) -> b, HashMap::new));
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public Boolean getValid() {
        return isValid;
    }

    public Boolean getExpired() {
        return expired;
    }
}
