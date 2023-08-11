package com.bompotis.netcheck.api.model;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "notificationServices", itemRelation = "notificationService")
public class NotificationServiceModel extends RepresentationModel<NotificationServiceModel> {
    private final Boolean enabled;

    private final String name;

    public NotificationServiceModel(Boolean enabled, String name) {
        this.enabled = enabled;
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }
}
