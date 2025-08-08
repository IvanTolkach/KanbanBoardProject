package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface TaskEndpoint {
    @GetMapping(ApiEndpoints.Task.BASE)
    List<TaskDTOOutput> getTasks(
            @RequestBody TaskFilterDTO filter
    );

    @GetMapping(ApiEndpoints.Task.USERS_TASKS)
    List<UserTaskDTO> getUsersTasks(
            @RequestBody UserTaskFilterDTO filter
    );

    @PutMapping(ApiEndpoints.Task.BY_PROJECT_ID)
    ResponseEntity<TaskDTOOutput> createUpdateTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody TaskDTOInput taskDTOInput
    );

    @PutMapping(ApiEndpoints.Task.ATTACH_USER)
    ResponseEntity<UserTaskDTO> attachUser(
            @PathVariable UUID taskId,
            @PathVariable UUID userId,
            @RequestBody(required = false) UserTaskDTO userTaskDTO
    );
}
