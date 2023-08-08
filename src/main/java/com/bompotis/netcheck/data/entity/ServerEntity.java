/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.bompotis.netcheck.data.entity;

import com.bompotis.netcheck.service.dto.Operation;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 4/9/20.
 */
@Entity
@Table(name = "server", indexes = {
        @Index(name = "server__id_password_idx", columnList = "id, password")
})
public class ServerEntity extends AbstractTimestampablePersistable<String>{

    @NonNull
    @Column(name = "server_name")
    private String serverName;

    @Column(name = "description")
    private String description;

    @NonNull
    @Column(name = "password")
    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="serverEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ServerMetricDefinitionEntity> serverMetricDefinitionEntities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="serverEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ServerMetricEntity> serverMetricEntities;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "server_domain",
            joinColumns = @JoinColumn(name = "server_id"),
            inverseJoinColumns = @JoinColumn(name = "domain"))
    private Set<DomainEntity> domainEntities;

    protected ServerEntity() {}

    public String getServerName() {
        return serverName;
    }

    public Set<ServerMetricEntity> getServerMetricEntities() {
        return serverMetricEntities;
    }

    public Set<DomainEntity> getDomainEntities() {
        return domainEntities;
    }

    @Transient
    public boolean passwordMatches(PasswordEncoder passwordEncoder, String password) {
        return passwordEncoder.matches(password, this.password);
    }

    public String getDescription() {
        return description;
    }

    public Set<ServerMetricDefinitionEntity> getServerMetricDefinitionEntities() {
        return serverMetricDefinitionEntities;
    }

    public static class Builder implements EntityBuilder<ServerEntity> {
        private final PasswordEncoder passwordEncoder;
        private String serverName;
        private String password;
        private String description;
        private Set<ServerMetricEntity> serverMetricEntities;
        private Set<DomainEntity> domainEntities;
        private Set<ServerMetricDefinitionEntity> serverMetricDefinitionEntities;

        public Builder(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder password(String password) {
            this.password = passwordEncoder.encode(password);
            return this;
        }

        public Builder domainEntities(Set<DomainEntity> domainEntities) {
            this.domainEntities = domainEntities;
            return this;
        }

        public Builder serverMetricDefinitionEntities(Set<ServerMetricDefinitionEntity> serverMetricDefinitionEntities) {
            this.serverMetricDefinitionEntities = serverMetricDefinitionEntities;
            return this;
        }

        public Builder serverMetricEntities(Set<ServerMetricEntity> serverMetricEntities) {
            this.serverMetricEntities = serverMetricEntities;
            return this;
        }

        public ServerEntity build() {
            return new ServerEntity(this);
        }
    }

    public static class Updater implements OperationUpdater<ServerEntity> {
        private final PasswordEncoder passwordEncoder;
        private final String id;
        private String serverName;
        private String password;
        private final Set<ServerMetricEntity> serverMetricEntities;
        private final Set<ServerMetricDefinitionEntity> serverMetricDefinitionEntities;
        private final Set<DomainEntity> domainEntities;
        private final Date createdAt;
        private String description;

        public Updater(ServerEntity entity, PasswordEncoder passwordEncoder) {
            this.id = entity.getId();
            this.serverName = entity.serverName;
            this.password = entity.password;
            this.serverMetricEntities = entity.serverMetricEntities;
            this.serverMetricDefinitionEntities = entity.serverMetricDefinitionEntities;
            this.domainEntities = entity.domainEntities;
            this.passwordEncoder = passwordEncoder;
            this.createdAt = entity.createdAt;
            this.description = entity.description;
        }

        public Updater withUpdatedValues(List<Operation> operations) {
            operations.forEach(this::processOperation);
            return this;
        }

        public void removeField(String field, String path) {
            switch (field) {
                case "serverName":
                    this.serverName = "";
                    break;
                case "description":
                    this.description = null;
                    break;
                case "password":
                    throw new IllegalArgumentException("Password field cannot be deleted");
                default:
                    throw new IllegalArgumentException("Invalid property for removal: " + field);
            }
        }

        public void updateField(String field, String path, String value) {
            switch (field) {
                case "serverName":
                    this.serverName = value;
                    break;
                case "description":
                    this.description = value;
                    break;
                case "password":
                    if (Optional.ofNullable(value).isEmpty() || value.isBlank() || value.length() < 8 ) {
                        throw new IllegalArgumentException("Password cannot be empty or less than 8 characters");
                    }
                    this.password = passwordEncoder.encode(value);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid property to update/add: " + field);
            }
        }

        public ServerEntity build() {
            return new ServerEntity(this);
        }
    }

    private ServerEntity(Builder b) {
        this.serverName = b.serverName;
        this.password = b.password;
        this.serverMetricEntities = b.serverMetricEntities;
        this.serverMetricDefinitionEntities = b.serverMetricDefinitionEntities;
        this.domainEntities = b.domainEntities;
        this.description = b.description;
    }

    private ServerEntity(Updater b) {
        this.id = b.id;
        this.serverName = b.serverName;
        this.password = b.password;
        this.description = b.description;
        this.serverMetricEntities = Set.copyOf(b.serverMetricEntities);
        this.serverMetricDefinitionEntities = Set.copyOf(b.serverMetricDefinitionEntities);
        this.domainEntities = Set.copyOf(b.domainEntities);
        this.createdAt = b.createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerEntity that = (ServerEntity) o;
        return serverName.equals(that.serverName) &&
                password.equals(that.password) &&
                Objects.equals(serverMetricEntities, that.serverMetricEntities) &&
                Objects.equals(domainEntities, that.domainEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
