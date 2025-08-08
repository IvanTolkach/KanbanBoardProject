package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskTagDTO {
    private UUID id;

    @NotNull(message = "Task id cannot be empty")
    private UUID taskId;

    @NotNull(message = "Tag id cannot be empty")
    private UUID tagId;
}
