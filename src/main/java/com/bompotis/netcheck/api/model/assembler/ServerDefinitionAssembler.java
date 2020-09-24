package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.controller.ServerController;
import com.bompotis.netcheck.api.model.ServerDefinitionModel;
import com.bompotis.netcheck.service.dto.ServerDefinitionDto;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

/**
 * Created by Kyriakos Bompotis on 9/9/20.
 */
public class ServerDefinitionAssembler extends RepresentationModelAssemblerSupport<ServerDefinitionDto, ServerDefinitionModel> {

    public ServerDefinitionAssembler() {
        super(ServerController.class, ServerDefinitionModel.class);
    }

    @Override
    public ServerDefinitionModel toModel(ServerDefinitionDto entity) {
        return new ServerDefinitionModel(
                entity.getLabel(),
                entity.getFieldName(),
                entity.getSuffix(),
                entity.getValueType(),
                entity.getMetricKind(),
                entity.getExtendedType(),
                entity.getMinThreshold(),
                entity.getMaxThreshold(),
                entity.getNotify()
        );
    }

    public ServerDefinitionDto toDto(ServerDefinitionModel model) {
        return new ServerDefinitionDto.Builder()
                .label(model.getLabel())
                .fieldName(model.getFieldName())
                .suffix(model.getSuffix())
                .valueType(model.getValueType())
                .metricKind(model.getMetricKind())
                .extendedType(model.getExtendedType())
                .maxThreshold(model.getMaxThreshold())
                .minThreshold(model.getMinThreshold())
                .notify(model.getNotify())
                .build();
    }
}
