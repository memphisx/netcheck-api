package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.DomainsController;
import com.bompotis.netcheck.api.model.DomainModel;
import com.bompotis.netcheck.service.dto.DomainDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public class DomainModelAssembler extends PaginatedRepresentationModelAssemblerSupport<DomainDto, DomainModel> {
    public DomainModelAssembler() {
        super(DomainsController.class, DomainModel.class);
    }

    @Override
    public DomainModel toModel(DomainDto domainDto) {
        return new DomainModel(
                domainDto.getDomain(),
                new DomainCheckModelAssembler().toModel(domainDto.getLastDomainCheck()),
                domainDto.getCreatedAt()
        );
    }

    public CollectionModel<DomainModel> toCollectionModel(PaginatedDto<DomainDto> paginatedDomainsDto) throws IOException {
        var domainModels = new ArrayList<DomainModel>();
        for (var domain : paginatedDomainsDto.getDtoList()) {
            var domainModel = this.toModel(domain);
            domainModel.add(
                    linkTo(methodOn(DomainsController.class).getDomainStatus(domainModel.getDomain(), false)).withSelfRel()
            );
            domainModels.add(domainModel);
        }
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(DomainsController.class)
                .getDomains(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getSize()))
                .withSelfRel()
        );
        if (isValidPage(paginatedDomainsDto.getNumber(),paginatedDomainsDto.getTotalPages())) {
            if (isNotLastPage(paginatedDomainsDto.getNumber(), paginatedDomainsDto.getTotalPages())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(paginatedDomainsDto.getNumber()+1, paginatedDomainsDto.getSize()))
                        .withRel(IanaLinkRelations.NEXT)
                );
            }
            if (isNotFirstPage(paginatedDomainsDto.getNumber())) {
                links.add(linkTo(methodOn(DomainsController.class)
                        .getDomains(paginatedDomainsDto.getNumber()-1, paginatedDomainsDto.getSize()))
                        .withRel(IanaLinkRelations.PREVIOUS)
                );
            }
        }
        return PagedModel.of(
                domainModels,
                new PagedModel.PageMetadata(
                        domainModels.size(),
                        paginatedDomainsDto.getNumber(),
                        paginatedDomainsDto.getTotalElements(),
                        paginatedDomainsDto.getTotalPages()
                ),
                links
        );
    }
}
