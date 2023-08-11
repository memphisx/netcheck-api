package com.bompotis.netcheck.api.controller;

import com.bompotis.netcheck.api.model.DomainCheckModel;
import com.bompotis.netcheck.api.model.NotificationServiceModel;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationDto;
import com.bompotis.netcheck.scheduler.batch.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.*;

@RestController
@CrossOrigin(origins = {"${settings.cors.origin}"})
@RequestMapping(value = "/api/v1/notification")
@Tag(name = "Notification Test", description = "Operations for testing notification providers")
public class NotificationTestController {

    private final List<NotificationService> notificationServices;

    @Autowired
    public NotificationTestController(List<NotificationService> notificationServices) {
        this.notificationServices = notificationServices;
    }

    @Operation(summary = "Get available notification services")
    @GetMapping(produces={"application/hal+json"})
    public ResponseEntity<CollectionModel<NotificationServiceModel>> getNotificationServices() {
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(NotificationTestController.class).getNotificationServices()).withSelfRel());
        var services = new ArrayList<>(notificationServices.stream()
                .map(service -> new NotificationServiceModel(service.isEnabled(),service.name()))
                .toList());
        var pageMtd = new PagedModel.PageMetadata(
                services.size(),
                0,
                services.size(),
                1
        );
        return ok(PagedModel.of(services, pageMtd, links));
    }

    @Operation(summary = "Send test notification to service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test Notification successfully sent"),
            @ApiResponse(responseCode = "400", description = "Notification service is disabled", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notification service not found", content = @Content)})
    @PostMapping(produces={"application/hal+json"}, path = "/{notificationServiceName}")
    public ResponseEntity<DomainCheckModel> sendTestNotificationTo(
            @PathVariable("notificationServiceName") String notificationServiceName
    ) throws Exception {
        Optional<NotificationService> foundService = Optional.empty();
        for (var service: notificationServices) {
            if (service.name().equals(notificationServiceName)) {
                foundService = Optional.of(service);
                break;
            }
        }
        if (foundService.isEmpty()) {
            return notFound().build();
        }
        var notificationService = foundService.get();
        if (!notificationService.isEnabled()) {
            return badRequest().build();
        }
        notificationService.notify(new NotificationDto.Builder()
                .hostname("test")
                .connectionAccepted(true)
                .dnsResolves(true)
                .ipAddress("0.0.0.0")
                .redirectUri("")
                .responseTimeNs(0L)
                .statusCode(200)
                .timeCheckedOn(new Date())
                .type(NotificationDto.Type.HTTP)
                .build());
        return ok().build();
    }
}
