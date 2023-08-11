package com.bompotis.netcheck.api.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureEmbeddedDatabase
public class NotificationTestControllerTests {

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
    void shouldReturn200WithAllNotificationServices() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/notification")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/notification"))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andReturn()
                .getResponse();
    }

    @Test
    void shouldReturn200WhenSendingNotificationToAnEnabledService() throws Exception {
        wm.stubFor(any(urlPathEqualTo("/webhook"))
                .willReturn(aResponse().withStatus(200)));
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/notification/Webhook")
                        .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        wm.verify(1, postRequestedFor(urlEqualTo("/webhook")));
        var unmatched = wm.findUnmatchedRequests();
        assertThat(unmatched.getRequests().size()).isEqualTo(0);
    }

    @Test
    void shouldReturn400WhenSendingNotificationToADisabledService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/notification/Pushover")
                        .accept("application/hal+json"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        var unmatched = wm.findUnmatchedRequests();
        assertThat(unmatched.getRequests().size()).isEqualTo(0);
    }

    @Test
    void shouldReturn404WhenSendingNotificationToANonExistentService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/notification/InvalidService")
                        .accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
        var unmatched = wm.findUnmatchedRequests();
        assertThat(unmatched.getRequests().size()).isEqualTo(0);
    }
}
