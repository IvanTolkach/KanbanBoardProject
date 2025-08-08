package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnFilterDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface ColumnEndpoint {
    @GetMapping(ApiEndpoints.ProjectColumn.BASE)
    List<ProjectColumnDTOOutput> getProjectColumns(
            @RequestBody ProjectColumnFilterDTO filter
    );

    @PutMapping(ApiEndpoints.ProjectColumn.BY_PROJECT_ID)
    ResponseEntity<ProjectColumnDTOOutput> createUpdateColumn(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectColumnDTOInput projectColumn
    );

    @DeleteMapping(ApiEndpoints.ProjectColumn.BY_ID)
    ResponseEntity<Void> deleteColumn(
            @PathVariable UUID columnId
    );
}
