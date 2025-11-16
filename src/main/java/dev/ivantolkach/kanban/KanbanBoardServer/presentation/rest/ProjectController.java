package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.ProjectEndpoint;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ProjectController implements ProjectEndpoint {

    @Autowired
    private ProjectService projectService;

    @Override
    public List<ProjectDTOOutput> getProjects(ProjectFilterDTO filter) {
        return projectService.getProjectsByFilter(filter);
    }

    @Override
    public ProjectDTOOutput createUpdateProject(ProjectDTOInput project) {
        return projectService.createUpdateProject(project);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(UUID projectId) {
        projectService.deleteProject(projectId);
    }
}
