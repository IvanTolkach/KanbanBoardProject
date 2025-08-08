package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTOOutput {
    private UUID id;

    @NotEmpty(message = "Title cannot be empty")
    private String title;

    private String description;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
