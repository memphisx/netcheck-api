package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainMetricRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureEmbeddedDatabase
public class DomainsControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DomainCheckRepository domainCheckRepository;

    @Autowired
    private DomainMetricRepository domainMetricRepository;

    @Test
    void shouldReturn200WithNoResultsIfNoDomainsAreStored() throws Exception {
        domainRepository.deleteAll();
        var domains = domainRepository.findAll();
        assertThat(domains.size()).isEqualTo(0);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/domains?page=0&size=0&showLastChecks=true&filter=&sortBy=createdAt&desc=true"))
                .andExpect(jsonPath("$.page.size").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0))
                .andExpect(jsonPath("$.page.number").value(0))
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn404IfDomainDoesNotExist() throws Exception {
        var domain = domainRepository.findById("google.com");
        assertThat(domain.isPresent()).isFalse();
        var response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/domains/google.com").accept("application/hal+json")
                )
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void shouldReturn200WithDomain() throws Exception {
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test.com").build())
                .build()
        );
        assertThat(domainCheck.getDomain()).isEqualTo("test.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test.com")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        domainCheckRepository.delete(domainCheck);
    }

    @Test
    void shouldReturn200WithDomainHistory() throws Exception {
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test.com").build())
                .build()
        );
        assertThat(domainCheck.getDomain()).isEqualTo("test.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test.com/history")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.domainChecks[0].domain").value("test.com"))
                .andReturn()
                .getResponse();
        domainCheckRepository.delete(domainCheck);
        domainRepository.delete(domainCheck.getDomainEntity());
    }

    @Test
    void shouldReturn200WithDomainHistoricCheck() throws Exception {
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test.com").build())
                .build()
        );
        assertThat(domainCheck.getDomain()).isEqualTo("test.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test.com/history/" + domainCheck.getId())
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.domain").value("test.com"))
                .andReturn()
                .getResponse();
        domainCheckRepository.delete(domainCheck);
        domainRepository.delete(domainCheck.getDomainEntity());
    }

    @Test
    void shouldReturn404OnDomainHistoryWhenDomainDoesNotExist() throws Exception {
        var domain = domainRepository.findById("test.com");
        assertThat(domain.isPresent()).isFalse();
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test.com/history")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn404OnDomainHistoryCheckWhenDomainDoesNotExist() throws Exception {
        var domain = domainRepository.findById("test.com");
        assertThat(domain.isPresent()).isFalse();
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test.com/history/some-random-id")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn200WithDomainStates() throws Exception {
        var http = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTP")
                .build();
        var https = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTPS")
                .build();
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test2.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test2.com").build())
                .protocolCheckEntities(Set.of(http,https))
                .build()
        );
        assertThat(domainCheck.getDomain()).isEqualTo("test2.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/states")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.states[0].hostname").value("test2.com"))
                .andExpect(jsonPath("$._embedded.states[0].statusCode").value(200))
                .andExpect(jsonPath("$._embedded.states[0].reason").value("First Check"))
                .andExpect(jsonPath("$._embedded.states[0].dnsResolves").value(true))
                .andReturn()
                .getResponse();
        domainCheckRepository.delete(domainCheck);
        domainRepository.delete(domainCheck.getDomainEntity());
    }

    @Test
    void shouldReturn404OnWithDomainStatesWhenDomainIsNotStored() throws Exception {
        var domainCheck = domainCheckRepository.findById("test2.com");
        assertThat(domainCheck.isPresent()).isFalse();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/states")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn200WithHttpChecks() throws Exception {
        var http = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTP")
                .build();
        var https = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTPS")
                .build();
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test2.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test2.com").build())
                .protocolCheckEntities(Set.of(http,https))
                .build()
        );
        assertThat(domainCheck.getDomain()).isEqualTo("test2.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/http")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.checks[0].hostname").value("test2.com"))
                .andExpect(jsonPath("$._embedded.checks[0].statusCode").value(200))
                .andExpect(jsonPath("$._embedded.checks[0].responseTimeNs").value(10))
                .andExpect(jsonPath("$._embedded.checks[0].dnsResolves").value(true))
                .andExpect(jsonPath("$._embedded.checks[0].ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$._embedded.checks[0].protocol").value("HTTP"))
                .andExpect(jsonPath("$._embedded.checks[0].up").value(true))
                .andExpect(jsonPath("$._embedded.checks[1].up").doesNotExist())
                .andReturn()
                .getResponse();
        domainCheckRepository.delete(domainCheck);
        domainRepository.delete(domainCheck.getDomainEntity());
    }

    @Test
    void shouldReturn404OnWithDomainHttpChecksWhenDomainIsNotStored() throws Exception {
        var domainCheck = domainCheckRepository.findById("test2.com");
        assertThat(domainCheck.isPresent()).isFalse();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/http")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn200WithHttpsChecks() throws Exception {
        var http = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTP")
                .build();
        var https = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTPS")
                .build();
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test2.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test2.com").build())
                .protocolCheckEntities(Set.of(http,https))
                .build()
        );
        assertThat(domainCheck.getDomain()).isEqualTo("test2.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/https")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.checks[0].hostname").value("test2.com"))
                .andExpect(jsonPath("$._embedded.checks[0].statusCode").value(200))
                .andExpect(jsonPath("$._embedded.checks[0].responseTimeNs").value(10))
                .andExpect(jsonPath("$._embedded.checks[0].dnsResolves").value(true))
                .andExpect(jsonPath("$._embedded.checks[0].ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$._embedded.checks[0].protocol").value("HTTPS"))
                .andExpect(jsonPath("$._embedded.checks[0].up").value(true))
                .andExpect(jsonPath("$._embedded.checks[1].up").doesNotExist())
                .andReturn()
                .getResponse();
        domainCheckRepository.delete(domainCheck);
        domainRepository.delete(domainCheck.getDomainEntity());
    }

    @Test
    void shouldReturn404WithDomainHttpsChecksWhenDomainIsNotStored() throws Exception {
        var domainCheck = domainCheckRepository.findById("test2.com");
        assertThat(domainCheck.isPresent()).isFalse();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/https")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn200WithDomainMetrics() throws Exception {
        var http = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTP")
                .build();
        var https = new ProtocolCheckEntity.Builder()
                .connectionAccepted(true)
                .statusCode(200)
                .dnsResolves(true)
                .hostname("test2.com")
                .protocol("HTTPS")
                .build();
        var domainCheck = domainCheckRepository.save(new DomainCheckEntity.Builder()
                .domain("test2.com")
                .httpResponseTimeNs(10L)
                .httpIpAddress("127.0.0.1")
                .timeCheckedOn(new Date())
                .httpsResponseTimeNs(10L)
                .httpsIpAddress("127.0.0.1")
                .domainEntity(new DomainEntity.Builder().domain("test2.com").build())
                .protocolCheckEntities(Set.of(http,https))
                .build()
        );

        var endPeriod = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
        var startPeriod = Date.from(ZonedDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0).toInstant());
        var metric = domainMetricRepository.save(new DomainMetricEntity.Builder()
                .domain("test2.com")
                .avgResponseTimeNs(10L)
                .maxResponseTimeNs(10L)
                .minResponseTimeNs(10L)
                .successfulChecks(1)
                .totalChecks(1)
                .periodType(DomainMetricEntity.Period.MONTH)
                .protocol("HTTP")
                .endPeriod(endPeriod)
                .startPeriod(startPeriod)
                .build()
        );

        assertThat(domainCheck.getDomain()).isEqualTo("test2.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/metrics?protocol=http&period=this_month")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.metrics[0].protocol").value("HTTP"))
                .andExpect(jsonPath("$._embedded.metrics[0].totalChecks").value(1))
                .andExpect(jsonPath("$._embedded.metrics[0].successfulChecks").value(1))
                .andExpect(jsonPath("$._embedded.metrics[0].averageResponseTime").value(10))
                .andExpect(jsonPath("$._embedded.metrics[0].maxResponseTime").value(10))
                .andExpect(jsonPath("$._embedded.metrics[0].minResponseTime").value(10))
                .andExpect(jsonPath("$._embedded.metrics[1]").doesNotExist())
                .andReturn()
                .getResponse();
        domainMetricRepository.delete(metric);
        domainCheckRepository.delete(domainCheck);
        domainRepository.delete(domainCheck.getDomainEntity());
    }

    @Test
    void shouldReturn404WithDomainMetricsForHttpWhenDomainIsNotStored() throws Exception {
        var domainCheck = domainCheckRepository.findById("test2.com");
        assertThat(domainCheck.isPresent()).isFalse();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/domains/test2.com/metrics?protocol=http&period=this_month")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn200WhenAddingADomain() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/domains/google.com")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var domain = domainRepository.findById("google.com");
        assertThat(domain.isPresent()).isTrue();
        assertThat(domain.get().getDomain()).isEqualTo("google.com");
        domainRepository.delete(domain.get());
    }

    @Test
    void shouldReturn200WhenAddingADomainWithCustomOptions() throws Exception {
        var payload = "{\"checkFrequencyMinutes\": 1, \"endpoint\": \"/test\", \"httpPort\": 9000 , \"httpsPort\": 9001, \"timeoutMs\": 10000 }";
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/domains/google.com")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var domain = domainRepository.findById("google.com");
        assertThat(domain.isPresent()).isTrue();
        assertThat(domain.get().getDomain()).isEqualTo("google.com");
        assertThat(domain.get().getEndpoint()).isEqualTo("/test");
        assertThat(domain.get().getTimeoutMs()).isEqualTo(10000);
        assertThat(domain.get().getCheckFrequency()).isEqualTo(1);
        domainRepository.delete(domain.get());
    }

    @Test
    void shouldReturn200WhenDeletingAnExistingDomain() throws Exception {
        domainRepository.save(new DomainEntity.Builder().domain("google.com").build());
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/domains/google.com")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var domain = domainRepository.findById("google.com");
        assertThat(domain.isPresent()).isFalse();
    }

    @Test
    void shouldReturn200WhenPatchingAnExistingDomain() throws Exception {
        domainRepository.save(new DomainEntity.Builder().domain("google.com").build());
        var payload = """
                        [{
                            "op": "replace",
                            "field": "httpPort",
                            "value": "993"
                        },{
                            "op": "replace",
                            "field": "httpsPort",
                            "value": "994"
                        }]
                        """;
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/v1/domains/google.com")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var domain = domainRepository.findById("google.com");
        assertThat(domain.isPresent()).isTrue();
        assertThat(domain.get().getHttpPort()).isEqualTo(993);
        assertThat(domain.get().getHttpsPort()).isEqualTo(994);
        domainRepository.delete(domain.get());
    }

    @Test
    void shouldReturn404WhenPatchingAnExistingDomain() throws Exception {
        var payload = """
                        [{
                            "op": "replace",
                            "field": "httpPort",
                            "value": "993"
                        },{
                            "op": "replace",
                            "field": "httpsPort",
                            "value": "994"
                        }]
                        """;
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/v1/domains/google.com")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();

        var domain = domainRepository.findById("google.com");
        assertThat(domain.isPresent()).isFalse();
    }

    @Test
    void shouldReturn409WhenAddingAnExistingDomain() throws Exception {
        var domain = domainRepository.save(new DomainEntity.Builder().domain("google.com").build());
        assertThat(domain.getDomain()).isEqualTo("google.com");
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/domains/google.com")
                        .accept("application/hal+json")
                )
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse();
        domainRepository.delete(domain);
    }
}
