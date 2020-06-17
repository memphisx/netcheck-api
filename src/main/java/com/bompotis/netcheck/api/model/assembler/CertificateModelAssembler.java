package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.CertificateModel;
import com.bompotis.netcheck.service.dto.CertificateDetailsDto;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class CertificateModelAssembler extends RepresentationModelAssemblerSupport<CertificateDetailsDto, CertificateModel> {

    public CertificateModelAssembler() {
        super(DomainsController.class, CertificateModel.class);
    }

    @Override
    public CertificateModel toModel(CertificateDetailsDto certificateDetailsDto) {
        return new CertificateModel(
                certificateDetailsDto.getIssuedBy(),
                certificateDetailsDto.getIssuedFor(),
                certificateDetailsDto.getNotBefore(),
                certificateDetailsDto.getNotAfter(),
                certificateDetailsDto.isValid(),
                certificateDetailsDto.getExpired()
        );
    }
}
