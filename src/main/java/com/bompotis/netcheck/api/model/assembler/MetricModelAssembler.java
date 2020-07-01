package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.MetricModel;
import com.bompotis.netcheck.service.dto.MetricDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.ArrayList;

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
                entity.getMetricPeriodStart(),
                entity.getMetricPeriodEnd(),
                entity.getTotalChecks(),
                entity.getSuccessfulChecks(),
                entity.getAverageResponseTime(),
                entity.getMaxResponseTime(),
                entity.getMinResponseTime(),
                entity.getProtocol()
        );
    }

    public CollectionModel<MetricModel> toCollectionModel(PaginatedDto<MetricDto> paginatedMetricsDto, String domain, String protocol, String period) {
        final var metricModels = this.toCollectionModel(paginatedMetricsDto.getDtoList()).getContent();
        final var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomainsMetrics(domain, protocol, period, paginatedMetricsDto.getNumber(), paginatedMetricsDto.getSize()))
                .withSelfRel()
        );
        if (isValidPage(paginatedMetricsDto.getNumber(),paginatedMetricsDto.getTotalPages())) {
            if (isNotLastPage(paginatedMetricsDto.getNumber(), paginatedMetricsDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsMetrics(domain, protocol, period, paginatedMetricsDto.getNumber()+1, paginatedMetricsDto.getSize()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedMetricsDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomainsMetrics(domain, protocol, period, paginatedMetricsDto.getNumber()-1, paginatedMetricsDto.getSize()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                metricModels,
                new PagedModel.PageMetadata(
                        metricModels.size(),
                        paginatedMetricsDto.getNumber(),
                        paginatedMetricsDto.getTotalElements(),
                        paginatedMetricsDto.getTotalPages()
                ),
                links
        );
    }
}
