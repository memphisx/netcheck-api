package com.bompotis.netcheck.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import com.github.tomakehurst.wiremock.WireMockServer;

@AutoConfigureMockMvc
@SpringBootTest
public class CheckControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    private final WireMockServer wm = new WireMockServer(options().port(9000).httpsPort(9001));

    @BeforeEach
    public void setup() {
        wm.start();
    }

    @AfterEach
    public void teardown() {
        wm.stop();
    }

    @Test
    void shouldReturn200ForAccessibleUrlOnGet() throws Exception {
        var response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/check/google.com").accept("application/hal+json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domain").value("google.com"))
                .andExpect(jsonPath("$.monitored").value(false))
                .andExpect(jsonPath("$.httpChecks[0].hostname").value("google.com"))
                .andExpect(jsonPath("$.httpChecks[0].statusCode").value(200))
                .andExpect(jsonPath("$.httpChecks[0].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[0].responseTimeNs").isNumber())
                .andExpect(jsonPath("$.httpChecks[0].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[0].ipAddress").isString())
                .andExpect(jsonPath("$.httpChecks[0].protocol").value("HTTP"))
                .andExpect(jsonPath("$.httpChecks[0].up").value(true))
                .andExpect(jsonPath("$.httpChecks[0].errorMessage").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].hostname").value("google.com"))
                .andExpect(jsonPath("$.httpChecks[1].statusCode").value(200))
                .andExpect(jsonPath("$.httpChecks[1].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[1].responseTimeNs").isNumber())
                .andExpect(jsonPath("$.httpChecks[1].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[1].ipAddress").isString())
                .andExpect(jsonPath("$.httpChecks[1].protocol").value("HTTPS"))
                .andExpect(jsonPath("$.httpChecks[1].up").value(true))
                .andExpect(jsonPath("$.httpChecks[1].errorMessage").isEmpty())
                .andExpect(jsonPath("$.issuerCertificate.issuedBy").exists())
                .andExpect(jsonPath("$.issuerCertificate.issuedFor").exists())
                .andExpect(jsonPath("$.issuerCertificate.notBefore").exists())
                .andExpect(jsonPath("$.issuerCertificate.notAfter").exists())
                .andExpect(jsonPath("$.issuerCertificate.expired").value(false))
                .andExpect(jsonPath("$.issuerCertificate.valid").value(true))
                .andExpect(jsonPath("$.caCertificates[0].issuedBy").exists())
                .andExpect(jsonPath("$.caCertificates[0].issuedFor").exists())
                .andExpect(jsonPath("$.caCertificates[0].notBefore").exists())
                .andExpect(jsonPath("$.caCertificates[0].notAfter").exists())
                .andExpect(jsonPath("$.caCertificates[0].expired").value(false))
                .andExpect(jsonPath("$.caCertificates[0].valid").value(true))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/domains/google.com"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturn200ForInAccessibleUrlOnGet() throws Exception {
        var response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/check/127.0.0.1").accept("application/hal+json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domain").value("127.0.0.1"))
                .andExpect(jsonPath("$.monitored").value(false))
                .andExpect(jsonPath("$.httpChecks[0].hostname").value("127.0.0.1"))
                .andExpect(jsonPath("$.httpChecks[0].statusCode").isEmpty())
                .andExpect(jsonPath("$.httpChecks[0].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[0].responseTimeNs").isEmpty())
                .andExpect(jsonPath("$.httpChecks[0].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[0].ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$.httpChecks[0].protocol").value("HTTP"))
                .andExpect(jsonPath("$.httpChecks[0].up").value(false))
                .andExpect(jsonPath("$.httpChecks[0].errorMessage").value("Connection refused"))
                .andExpect(jsonPath("$.httpChecks[1].hostname").value("127.0.0.1"))
                .andExpect(jsonPath("$.httpChecks[1].statusCode").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[1].responseTimeNs").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[1].ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$.httpChecks[1].protocol").value("HTTPS"))
                .andExpect(jsonPath("$.httpChecks[1].up").value(false))
                .andExpect(jsonPath("$.httpChecks[1].errorMessage").value("Connection refused"))
                .andExpect(jsonPath("$.issuerCertificate").isEmpty())
                .andExpect(jsonPath("$.caCertificates").value(empty()))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/domains/127.0.0.1"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturn200ForAccessibleUrlOnPost() throws Exception {
        var payload = "{\"endpoint\": \"/login\"}";
        var response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/check/github.com")
                                .accept("application/hal+json")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domain").value("github.com"))
                .andExpect(jsonPath("$.monitored").value(false))
                .andExpect(jsonPath("$.httpChecks[0].hostname").value("github.com"))
                .andExpect(jsonPath("$.httpChecks[0].statusCode").value(301))
                .andExpect(jsonPath("$.httpChecks[0].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[0].responseTimeNs").isNumber())
                .andExpect(jsonPath("$.httpChecks[0].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[0].ipAddress").isString())
                .andExpect(jsonPath("$.httpChecks[0].protocol").value("HTTP"))
                .andExpect(jsonPath("$.httpChecks[0].redirectUri").value("https://github.com/login"))
                .andExpect(jsonPath("$.httpChecks[0].up").value(true))
                .andExpect(jsonPath("$.httpChecks[0].errorMessage").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].hostname").value("github.com"))
                .andExpect(jsonPath("$.httpChecks[1].statusCode").value(200))
                .andExpect(jsonPath("$.httpChecks[1].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[1].responseTimeNs").isNumber())
                .andExpect(jsonPath("$.httpChecks[1].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[1].ipAddress").isString())
                .andExpect(jsonPath("$.httpChecks[1].protocol").value("HTTPS"))
                .andExpect(jsonPath("$.httpChecks[1].up").value(true))
                .andExpect(jsonPath("$.httpChecks[1].errorMessage").isEmpty())
                .andExpect(jsonPath("$.issuerCertificate.issuedBy").exists())
                .andExpect(jsonPath("$.issuerCertificate.issuedFor").exists())
                .andExpect(jsonPath("$.issuerCertificate.notBefore").exists())
                .andExpect(jsonPath("$.issuerCertificate.notAfter").exists())
                .andExpect(jsonPath("$.issuerCertificate.expired").value(false))
                .andExpect(jsonPath("$.issuerCertificate.valid").value(true))
                .andExpect(jsonPath("$.caCertificates[0].issuedBy").exists())
                .andExpect(jsonPath("$.caCertificates[0].issuedFor").exists())
                .andExpect(jsonPath("$.caCertificates[0].notBefore").exists())
                .andExpect(jsonPath("$.caCertificates[0].notAfter").exists())
                .andExpect(jsonPath("$.caCertificates[0].expired").value(false))
                .andExpect(jsonPath("$.caCertificates[0].valid").value(true))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/domains/github.com"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturn200For404ResponseOnPost() throws Exception {
        wm.stubFor(any(urlPathEqualTo("/404"))
                .willReturn(aResponse().withStatus(404)));

        var payload = "{\"endpoint\": \"/404\", \"httpPort\": 9000, \"httpsPort\": 9001 }";
        var response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/check/127.0.0.1")
                                .accept("application/hal+json")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domain").value("127.0.0.1"))
                .andExpect(jsonPath("$.monitored").value(false))
                .andExpect(jsonPath("$.httpChecks[0].hostname").value("127.0.0.1"))
                .andExpect(jsonPath("$.httpChecks[0].statusCode").value(404))
                .andExpect(jsonPath("$.httpChecks[0].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[0].responseTimeNs").isNumber())
                .andExpect(jsonPath("$.httpChecks[0].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[0].ipAddress").isString())
                .andExpect(jsonPath("$.httpChecks[0].protocol").value("HTTP"))
                .andExpect(jsonPath("$.httpChecks[0].redirectUri").doesNotExist())
                .andExpect(jsonPath("$.httpChecks[0].up").value(false))
                .andExpect(jsonPath("$.httpChecks[0].errorMessage").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].hostname").value("127.0.0.1"))
                .andExpect(jsonPath("$.httpChecks[1].statusCode").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].checkedOn").value(startsWith(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(LocalDateTime.now(ZoneOffset.UTC)))))
                .andExpect(jsonPath("$.httpChecks[1].responseTimeNs").isEmpty())
                .andExpect(jsonPath("$.httpChecks[1].dnsResolves").value(true))
                .andExpect(jsonPath("$.httpChecks[1].ipAddress").isString())
                .andExpect(jsonPath("$.httpChecks[1].protocol").value("HTTPS"))
                .andExpect(jsonPath("$.httpChecks[1].up").value(false))
                .andExpect(jsonPath("$.httpChecks[1].errorMessage").value("Unsupported or unrecognized SSL message"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/domains/127.0.0.1"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
