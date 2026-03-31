package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskFilterDTO {

    private UUID id;

    private UUID columnId;

    @NotEmpty(message = "Title cannot be empty")
    private String title;

    private String description;

    private LocalDate plannedDueDateFrom;

    private LocalDate plannedDueDateTo;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    private LocalDateTime createdAtFrom;

    private LocalDateTime createdAtTo;

    private LocalDateTime updatedAtFrom;

    private LocalDateTime updatedAtTo;
}
