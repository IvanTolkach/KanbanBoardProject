package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.ColumnEndpoint;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.ProjectColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ColumnController implements ColumnEndpoint {

    @Autowired
    ProjectColumnService projectColumnService;

    @Override
    public List<ProjectColumnDTOOutput> getProjectColumns(ProjectColumnFilterDTO filter) {
        return projectColumnService.getProjectColumnsByFilter(filter);
    }

    @Override
    public ResponseEntity<ProjectColumnDTOOutput> createUpdateColumn(UUID projectId, ProjectColumnDTOInput projectColumn) {
        return ResponseEntity.ok(projectColumnService.createUpdateProjectColumn(projectId, projectColumn));
    }

    @Override
    public ResponseEntity<Void> deleteColumn(UUID columnId) {
        projectColumnService.deleteProjectColumn(columnId);
        return ResponseEntity.noContent().build();
    }
}
