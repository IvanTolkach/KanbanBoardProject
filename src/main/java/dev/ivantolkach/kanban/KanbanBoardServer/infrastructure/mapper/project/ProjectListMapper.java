package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.project;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProjectMapper.class)
public interface ProjectListMapper {
    List<ProjectDTOOutput> toDTOList(List<Project> projects);

    List<Project> toProjectList(List<ProjectDTOInput> projectDTOInputs);
}
