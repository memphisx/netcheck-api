package com.bompotis.netcheck.service.dto;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class PaginatedMetricsDto extends PaginatedDto{
    private final List<MetricDto> httpMetrics;
    private final List<MetricDto> httpsMetrics;

    public PaginatedMetricsDto(List<MetricDto> httpMetrics,
                               List<MetricDto> httpsMetrics,
                               long totalElements,
                               int totalPages,
                               int number,
                               int size) {
        super(totalElements, totalPages, number, size);
        this.httpMetrics = httpMetrics;
        this.httpsMetrics = httpsMetrics;
    }

    public List<MetricDto> getHttpMetrics() {
        return httpMetrics;
    }

    public List<MetricDto> getHttpsMetrics() {
        return httpsMetrics;
    }
}
