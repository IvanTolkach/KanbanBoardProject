package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProjectColumnMapper.class)
public interface ProjectColumnListMapper {
    List<ProjectColumnDTOOutput> toDTOList(List<ProjectColumn> projectColumns);

    List<ProjectColumn> toProjectColumnList(List<ProjectColumnDTOInput> projectColumnDTOInputs);
}
