package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class DomainsControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DomainCheckRepository domainCheckRepository;

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
