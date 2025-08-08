package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskFilterDTO {
    private UUID id;

    @NotNull(message = "User id cannot be empty")
    private UUID userId;

    @NotNull(message = "Task id cannot be empty")
    private UUID taskId;

    private Integer timeConsumedFrom;

    private Integer timeConsumedTo;

    private Boolean isAssigned;
}
