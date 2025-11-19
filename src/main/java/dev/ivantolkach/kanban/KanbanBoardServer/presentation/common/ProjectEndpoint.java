package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectFilterDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface ProjectEndpoint {
    @GetMapping(ApiEndpoints.Project.BASE)
    List<ProjectDTOOutput> getProjects(
            @RequestBody ProjectFilterDTO filter
    );

    @PutMapping(ApiEndpoints.Project.BASE)
    ProjectDTOOutput createUpdateProject(
            @Valid @RequestBody ProjectDTOInput project
    );

    @DeleteMapping(ApiEndpoints.Project.BY_ID)
    void deleteProject(
            @PathVariable UUID projectId
    );
}
