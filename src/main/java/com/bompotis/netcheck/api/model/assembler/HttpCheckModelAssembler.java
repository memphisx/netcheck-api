package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.HttpCheckModel;
import com.bompotis.netcheck.service.dto.HttpCheckDto;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class HttpCheckModelAssembler extends RepresentationModelAssemblerSupport<HttpCheckDto, HttpCheckModel> {

    public HttpCheckModelAssembler() {
        super(DomainsController.class, HttpCheckModel.class);
    }

    @Override
    public HttpCheckModel toModel(HttpCheckDto httpCheckDto) {
        return new HttpCheckModel(
                httpCheckDto.getHostname(),
                httpCheckDto.getStatusCode(),
                httpCheckDto.getTimeCheckedOn(),
                httpCheckDto.getResponseTimeNs(),
                httpCheckDto.getDnsResolved(),
                httpCheckDto.getIpAddress(),
                httpCheckDto.getProtocol()
        );
    }
}
