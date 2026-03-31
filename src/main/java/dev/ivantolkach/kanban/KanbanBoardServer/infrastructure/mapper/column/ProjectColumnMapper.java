package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectColumnMapper {
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "default", target = "default")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "updatedBy.id", target = "updatedBy")
    ProjectColumnDTOOutput toDTO(ProjectColumn projectColumn);

    @Mapping(source = "projectId", target = "project.id")
    @Mapping(source = "default", target = "default")
    ProjectColumn toProjectColumn(ProjectColumnDTOInput projectColumnDTOInput);
}
