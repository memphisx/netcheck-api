package com.bompotis.netcheck.scheduler.batch.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class PushoverRestClient {

    private record PushoverErrorResponse(HttpStatusCode statusCode) {}

    public static final String PUSH_MESSAGE_URL = "https://api.pushover.net/1/messages.json";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(PushoverRestClient.class);

    public void pushMessage(PushoverMessage msg) {

        final var post = WebClient.create().post().uri(PUSH_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        final var valueMap = new LinkedMultiValueMap<String, String>();

        valueMap.add("token", msg.getApiToken());
        valueMap.add("user", msg.getUserId());
        if (msg.getHtmlMessage() != null && !msg.getHtmlMessage().trim().isEmpty()) {
            valueMap.add("message", msg.getHtmlMessage());
        } else {
            valueMap.add("message", msg.getMessage());
        }

        addPairIfNotNull(valueMap, "title", msg.getTitle());

        addPairIfNotNull(valueMap, "url", msg.getUrl());
        addPairIfNotNull(valueMap, "url_title", msg.getTitleForURL());

        addPairIfNotNull(valueMap, "device", msg.getDevice());
        addPairIfNotNull(valueMap, "timestamp", msg.getTimestamp());
        addPairIfNotNull(valueMap, "sound", msg.getSound());

        if (msg.getHtmlMessage() != null && !msg.getHtmlMessage().trim().isEmpty()) {
            addPairIfNotNull(valueMap, "html", "1");
        }

        if (!MessagePriority.NORMAL.equals(msg.getPriority())) {
            addPairIfNotNull(valueMap, "priority", msg.getPriority());
        }

        var ref = new Object() {
            String requestId;
        };
        post.body(BodyInserters.fromFormData(valueMap)).exchangeToMono(response -> {
            var statusCode = response.statusCode();
            if (statusCode.equals(HttpStatus.OK)) {
                ref.requestId = response.headers().header(REQUEST_ID_HEADER).get(0);
                return response.bodyToMono(PushoverResponse.class);
            }
            return Mono.just(new PushoverErrorResponse(statusCode));
        }).single().subscribe(result -> {
            if (result.getClass().equals(PushoverResponse.class)) {
                var r = (PushoverResponse) result;
                log.info("Pushover notification response: status {}, requestId {}.",r.status, ref.requestId);
            } else if (result.getClass().equals(PushoverErrorResponse.class)) {
                var r = (PushoverErrorResponse) result;
                log.error("Pushover request failed with status code {}.",r.statusCode.value());
            }
        });
    }


    private void addPairIfNotNull(MultiValueMap<String, String> multiValueMap, String key, Object value) {
        if (value != null) {
            multiValueMap.add(key, value.toString());
        }
    }

    public static class PushoverResponse {
        int status;
    }
}
