package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.ServerController;
import com.bompotis.netcheck.api.model.ServerMetricModel;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import com.bompotis.netcheck.service.dto.RequestOptionsDto;
import com.bompotis.netcheck.service.dto.ServerMetricDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 7/9/20.
 */
public class ServerMetricModelAssembler extends PaginatedRepresentationModelAssemblerSupport<ServerMetricDto, ServerMetricModel> {
    public ServerMetricModelAssembler() {
        super(ServerController.class, ServerMetricModel.class);
    }

    @Override
    public ServerMetricModel toModel(ServerMetricDto entity) {
        return new ServerMetricModel(
                entity.getId(),
                entity.getMetrics(),
                entity.getCollectedAt()
        );
    }

    public CollectionModel<ServerMetricModel> toCollectionModel(PaginatedDto<ServerMetricDto> paginateServerMetricDtos, RequestOptionsDto requestOptionsDto, String serverId) {
        var metrics = new ArrayList<ServerMetricModel>();
        for (var entity: paginateServerMetricDtos.getDtoList()) {
            metrics.add(toModel(entity));
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(ServerController.class)
                .getServerMetrics(
                        serverId,
                        paginateServerMetricDtos.getNumber(),
                        paginateServerMetricDtos.getSize(),
                        requestOptionsDto.getSortBy(),
                        requestOptionsDto.getDesc()))
                .withSelfRel());
        if (isValidPage(paginateServerMetricDtos.getNumber(),paginateServerMetricDtos.getTotalPages())) {
            if (isNotLastPage(paginateServerMetricDtos.getNumber(), paginateServerMetricDtos.getTotalPages())) {
                links.add(linkTo(methodOn(ServerController.class)
                        .getServerMetrics(
                                serverId,
                                paginateServerMetricDtos.getNumber()+1,
                                paginateServerMetricDtos.getSize(),
                                requestOptionsDto.getSortBy(),
                                requestOptionsDto.getDesc()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotLastPage(paginateServerMetricDtos.getNumber(), paginateServerMetricDtos.getTotalPages())) {
                links.add(linkTo(methodOn(ServerController.class)
                        .getServerMetrics(
                                serverId,
                                paginateServerMetricDtos.getNumber()-1,
                                paginateServerMetricDtos.getSize(),
                                requestOptionsDto.getSortBy(),
                                requestOptionsDto.getDesc()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                metrics,
                new PagedModel.PageMetadata(
                        metrics.size(),
                        paginateServerMetricDtos.getNumber(),
                        paginateServerMetricDtos.getTotalElements(),
                        paginateServerMetricDtos.getTotalPages()),
                links
        );
    }

}
