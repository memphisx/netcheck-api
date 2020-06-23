package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.MetricModel;
import com.bompotis.netcheck.service.dto.MetricDto;
import com.bompotis.netcheck.service.dto.PaginatedMetricsDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class MetricModelAssembler extends PaginatedRepresentationModelAssemblerSupport<MetricDto, MetricModel> {
    public MetricModelAssembler() {
        super(DomainsController.class, MetricModel.class);
    }

    @Override
    public MetricModel toModel(MetricDto entity) {
        return new MetricModel(
                entity.getMetricPeriod(),
                entity.getUptimePercentage(),
                entity.getAverageResponseTime()
        );
    }

    public CollectionModel<MetricModel> toCollectionModel(PaginatedMetricsDto paginatedMetricDto, String domain) {
        var metricModels = paginatedMetricDto.getHttpMetrics().stream().map(this::toModel).collect(Collectors.toCollection(ArrayList::new));
        var links = new ArrayList<Link>();
        var method = methodOn(DomainsController.class)
                .getDomainsMetrics(domain, paginatedMetricDto.getNumber(), paginatedMetricDto.getSize());
        links.add(linkTo(method)
                .withSelfRel()
        );
        if (isValidPage(paginatedMetricDto.getNumber(),paginatedMetricDto.getTotalPages())) {
            if (isNotLastPage(paginatedMetricDto.getNumber(), paginatedMetricDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsMetrics(domain,paginatedMetricDto.getNumber()+1, paginatedMetricDto.getSize()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedMetricDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsMetrics(domain,paginatedMetricDto.getNumber()-1, paginatedMetricDto.getSize()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                metricModels,
                new PagedModel.PageMetadata(
                        metricModels.size(),
                        paginatedMetricDto.getNumber(),
                        paginatedMetricDto.getTotalElements(),
                        paginatedMetricDto.getTotalPages()
                ),
                links
        );
    }
}
