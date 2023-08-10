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
package com.bompotis.netcheck.service;

import com.bompotis.netcheck.api.exception.EntityNotFoundException;
import com.bompotis.netcheck.api.exception.UnauthorizedStatusException;
import com.bompotis.netcheck.data.entity.ServerEntity;
import com.bompotis.netcheck.data.entity.ServerMetricDefinitionEntity;
import com.bompotis.netcheck.data.entity.ServerMetricEntity;
import com.bompotis.netcheck.data.repository.ServerMetricDefinitionRepository;
import com.bompotis.netcheck.data.repository.ServerMetricRepository;
import com.bompotis.netcheck.data.repository.ServerRepository;
import com.bompotis.netcheck.scheduler.batch.notification.ServerMetricEventDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import com.bompotis.netcheck.service.dto.RequestOptionsDto;
import com.bompotis.netcheck.service.dto.ServerDefinitionDto;
import com.bompotis.netcheck.service.dto.ServerDto;
import com.bompotis.netcheck.service.dto.ServerMetricDto;
import com.bompotis.netcheck.service.dto.ServerUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Kyriakos Bompotis on 4/9/20.
 */
@Service
public class ServerMetricsService extends AbstractService {

    /**
     * Secure random number generator
     */
    private final Random random = new SecureRandom();

    /**
     * Password Encoder
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JPA repository of the server entities
     */
    private final ServerRepository serverRepository;

    /**
     * JPA repository of the server metric entities
     */
    private final ServerMetricRepository serverMetricRepository;

    /**
     * JPA repository of the server metric definition entities
     */
    private final ServerMetricDefinitionRepository serverMetricDefinitionRepository;

    /**
     * Application Event Publisher
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Default amount of upper case letters an auto generated secure password is going to contain
     */
    private static final int PASSWORD_UPPER_CASE_LETTERS_AMOUNT = 8;

    /**
     * Default amount of lower case letter an auto generated secure password is going to contain
     */
    private static final int PASSWORD_LOWER_CASE_LETTERS_AMOUNT = 16;

    /**
     * Default amount of numbers that an auto generated secure password is going to contain
     */
    private static final int PASSWORD_NUMBERS_AMOUNT = 8;

    /**
     * Constructor
     * @param passwordEncoder a password encoder
     * @param serverRepository jpa repository of the server entities
     * @param serverMetricRepository jpa repository of the server metric entities
     * @param serverMetricDefinitionRepository jpa repository of the server metric definition entities
     * @param eventPublisher application event publisher
     */
    @Autowired
    public ServerMetricsService(PasswordEncoder passwordEncoder,
                                ServerRepository serverRepository,
                                ServerMetricRepository serverMetricRepository,
                                ServerMetricDefinitionRepository serverMetricDefinitionRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.passwordEncoder = passwordEncoder;
        this.serverRepository = serverRepository;
        this.serverMetricRepository = serverMetricRepository;
        this.serverMetricDefinitionRepository = serverMetricDefinitionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Register a new server with the provided options
     * @param serverName the name of the server
     * @param description a description of the new server
     * @return a data transferable object with the server configuration including the generated id
     *         and the random password required to be used by the client in order to post metrics
     */
    public ServerDto registerServer(String serverName, String description) {
        var password = generateSecureRandomPassword();
        var serverEntity = serverRepository.save(
                new ServerEntity.Builder(passwordEncoder)
                        .serverName(serverName)
                        .description(description)
                        .password(password)
                        .build()
        );
        return new ServerDto.Builder()
                .dateAdded(serverEntity.getCreatedAt())
                .password(password)
                .description(serverEntity.getDescription())
                .serverId(serverEntity.getId())
                .serverName(serverEntity.getServerName())
                .build();
    }

    /**
     * Generate a secure random password
     * @return a randomly generated password
     */
    private String generateSecureRandomPassword() {
        Stream<Character> pwdStream =
                Stream.concat(getRandomNumbers(PASSWORD_NUMBERS_AMOUNT),
                Stream.concat(
                        getRandomLetters(PASSWORD_UPPER_CASE_LETTERS_AMOUNT, true),
                        getRandomLetters(PASSWORD_LOWER_CASE_LETTERS_AMOUNT, false)
                ));
        List<Character> charList = pwdStream.collect(Collectors.toList());
        Collections.shuffle(charList);
        return charList.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    /**
     * Generate a stream of random letter characters
     * @param count the amount of random letters to be generated
     * @param upperCase flag indicating if we want the letters to be upper case
     * @return a stream of random letters
     */
    private Stream<Character> getRandomLetters(int count, boolean upperCase) {
        IntStream characters = upperCase ?
                random.ints(count, 65, 90) :
                random.ints(count, 97, 122);
        return characters.mapToObj(data -> (char) data);
    }

    /**
     * Generate a stream of random numbers
     * @param count the amount of random numbers to be generated
     * @return a stream of random numbers
     */
    private Stream<Character> getRandomNumbers(int count) {
        return random.ints(count, 48, 57)
                .mapToObj(data -> (char) data);
    }

    /**
     * Get the configuration of all servers
     * @param options pagination, ordering and filtering options
     * @return a list of all registered servers and their configuration
     */
    public PaginatedDto<ServerDto> getServers(RequestOptionsDto options) {
        var servers = new ArrayList<ServerDto>();
        var paginatedQueryResult = options.getFilter().isBlank() ?
                serverRepository.findAll(options.getPageRequest()) :
                serverRepository.findAllFiltered(options.getFilter(), options.getPageRequest());

        paginatedQueryResult.forEach(
                (server) -> servers.add(new ServerDto.Builder()
                        .serverId(server.getId())
                        .serverName(server.getServerName())
                        .description(server.getDescription())
                        .serverDefinitionDtos(server.getServerMetricDefinitionEntities()
                                .stream()
                                .map(serverMetricDefinitionEntity -> new ServerDefinitionDto.Builder()
                                        .metricKind(serverMetricDefinitionEntity.getMetricKind().name())
                                        .valueType(serverMetricDefinitionEntity.getValueType().name())
                                        .suffix(serverMetricDefinitionEntity.getSuffix())
                                        .label(serverMetricDefinitionEntity.getLabel())
                                        .fieldName(serverMetricDefinitionEntity.getFieldName())
                                        .extendedType(Optional
                                                .ofNullable(serverMetricDefinitionEntity.getExtendedType())
                                                .map(Enum::name)
                                                .orElse(null))
                                        .minThreshold(serverMetricDefinitionEntity.getMinThreshold())
                                        .maxThreshold(serverMetricDefinitionEntity.getMaxThreshold())
                                        .notify(serverMetricDefinitionEntity.isNotify())
                                        .build())
                                .collect(Collectors.toList()))
                        .dateAdded(server.getCreatedAt())
                        .build()
                )
        );

        return new PaginatedDto<>(
                servers,
                paginatedQueryResult.getTotalElements(),
                paginatedQueryResult.getTotalPages(),
                paginatedQueryResult.getNumber(),
                paginatedQueryResult.getNumberOfElements()
        );
    }

    /**
     * Get the configuration of a specific server
     * @param id the id of the server
     * @return A data transferable object containing the server configuration
     * @throws EntityNotFoundException when no server is found with the provided id
     */
    public ServerDto getServer(String id) throws EntityNotFoundException {
        return serverRepository.findById(id).map(
                server -> new ServerDto.Builder()
                        .dateAdded(server.getCreatedAt())
                        .serverName(server.getServerName())
                        .description(server.getDescription())
                        .serverId(server.getId())
                        .serverDefinitionDtos(
                                server.getServerMetricDefinitionEntities()
                                        .stream()
                                        .map(serverMetricDefinitionEntity -> new ServerDefinitionDto.Builder()
                                                .metricKind(serverMetricDefinitionEntity.getMetricKind().name())
                                                .valueType(serverMetricDefinitionEntity.getValueType().name())
                                                .suffix(serverMetricDefinitionEntity.getSuffix())
                                                .label(serverMetricDefinitionEntity.getLabel())
                                                .fieldName(serverMetricDefinitionEntity.getFieldName())
                                                .extendedType(Optional
                                                        .ofNullable(serverMetricDefinitionEntity.getExtendedType())
                                                        .map(Enum::name)
                                                        .orElse(null))
                                                .minThreshold(serverMetricDefinitionEntity.getMinThreshold())
                                                .maxThreshold(serverMetricDefinitionEntity.getMaxThreshold())
                                                .notify(serverMetricDefinitionEntity.isNotify())
                                                .build())
                                        .collect(Collectors.toList()))
                        .build()
        ).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * Store metrics for the provided authenticated server
     * @param serverMetricDto data transferable object containing the metrics and the authentication credentials
     * @throws UnauthorizedStatusException when the credentials provided are invalid
     */
    public void addServerMetrics(ServerMetricDto serverMetricDto) throws UnauthorizedStatusException {
        var server = serverRepository
                .findById(serverMetricDto.getServerId())
                .filter(serverEntity -> serverEntity.passwordMatches(passwordEncoder, serverMetricDto.getPassword()))
                .orElseThrow(UnauthorizedStatusException::new);

        var entity = serverMetricRepository.save(new ServerMetricEntity.Builder()
                .serverEntity(server)
                .collectedAt(serverMetricDto.getCollectedAt())
                .metrics(serverMetricDto.getMetrics())
                .build());
        eventPublisher.publishEvent(new ServerMetricEventDto(this, serverMetricDto.toServerMetricEvent(entity.getId())));
    }

    /**
     * Get Server Metrics
     * @param serverId the id of the server the metrics belong to
     * @param options pagination and sorting options
     * @return the metrics of the server with the id and options that were provided
     */
    public PaginatedDto<ServerMetricDto> getServerMetrics(String serverId, RequestOptionsDto options) {
        var serverMetrics = new ArrayList<ServerMetricDto>();
        var paginatedQueryResult = serverMetricRepository.findAll(options.getPageRequest());

        paginatedQueryResult.forEach(metric -> serverMetrics
                .add(new ServerMetricDto.Builder(serverId)
                        .collectedAt(metric.getCollectedAt())
                        .metrics(metric.getMetrics())
                        .id(metric.getId())
                        .build())
        );

        return new PaginatedDto<>(
                serverMetrics,
                paginatedQueryResult.getTotalElements(),
                paginatedQueryResult.getTotalPages(),
                paginatedQueryResult.getNumber(),
                paginatedQueryResult.getNumberOfElements()
        );
    }

    /**
     * Delete a server
     * @param serverId the id of the server that is going to be deleted
     * @throws EntityNotFoundException if no entity found with the provided domain in the dto
     */
    public void deleteServer(String serverId) throws EntityNotFoundException {
        serverRepository.delete(serverRepository.findById(serverId).orElseThrow(EntityNotFoundException::new));
    }

    /**
     * Update the configuration of a server
     * @param serverUpdateDto data transferable object containing the updated values for the server config
     * @throws EntityNotFoundException if no entity found with the provided domain in the dto
     */
    public void updateServerConfig(ServerUpdateDto serverUpdateDto) throws EntityNotFoundException {
        serverRepository.save(
                new ServerEntity
                        .Updater(
                                serverRepository
                                        .findById(serverUpdateDto.getServerId())
                                        .orElseThrow(EntityNotFoundException::new),
                                passwordEncoder)
                        .withUpdatedValues(serverUpdateDto.getOperations())
                        .build()
        );
    }

    /**
     * Add or update metric definitions for a server
     * @param serverId id of the server on which we want the definitions to be assigned
     * @param serverDefinitionDtos list of data transferable objects containing metric definitions
     * @throws EntityNotFoundException when no server is found with the provided id
     */
    public void addServerMetricDefinitions(
            String serverId,
            List<ServerDefinitionDto> serverDefinitionDtos) throws EntityNotFoundException {
        var serverEntity = serverRepository.findById(serverId).orElseThrow(EntityNotFoundException::new);
        var definitions = serverDefinitionDtos
                .stream()
                .map( serverDefinitionDto -> new ServerMetricDefinitionEntity.Builder()
                        .fieldName(serverDefinitionDto.getFieldName())
                        .label(serverDefinitionDto.getLabel())
                        .metricKind(serverDefinitionDto.getMetricKind())
                        .serverEntity(serverEntity)
                        .suffix(serverDefinitionDto.getSuffix())
                        .valueType(serverDefinitionDto.getValueType())
                        .extendedType(serverDefinitionDto.getExtendedType())
                        .maxThreshold(serverDefinitionDto.getMaxThreshold())
                        .minThreshold(serverDefinitionDto.getMinThreshold())
                        .notify(serverDefinitionDto.getNotify())
                        .build())
                .collect(Collectors.toList());
        serverMetricDefinitionRepository.saveAll(definitions);
    }

    /**
     * Add or update a metric definition for a server
     * @param serverId id of the server on which we want the definitions to be assigned
     * @param serverDefinitionDto data transferable objects containing metric definitions
     * @throws EntityNotFoundException when no server is found with the provided id
     */
    public void upsertServerMetricDefinition(
            String serverId,
            ServerDefinitionDto serverDefinitionDto
    ) throws EntityNotFoundException {
        var serverEntity = serverRepository.findById(serverId).orElseThrow(EntityNotFoundException::new);
        serverMetricDefinitionRepository.save(
                new ServerMetricDefinitionEntity.Builder()
                .fieldName(serverDefinitionDto.getFieldName())
                .label(serverDefinitionDto.getLabel())
                .metricKind(serverDefinitionDto.getMetricKind())
                .serverEntity(serverEntity)
                .suffix(serverDefinitionDto.getSuffix())
                .valueType(serverDefinitionDto.getValueType())
                .build()
        );
    }
}
