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
