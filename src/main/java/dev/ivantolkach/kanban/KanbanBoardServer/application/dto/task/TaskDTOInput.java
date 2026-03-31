package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTOInput {
    private UUID id;

    private UUID columnId;

    private String title;

    private String description;

    private LocalDate plannedDueDate;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;

    private Map<String, String> parameters;
}
