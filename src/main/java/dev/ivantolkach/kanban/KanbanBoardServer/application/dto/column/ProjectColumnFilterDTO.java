package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectColumnFilterDTO {
    private UUID id;

    private UUID projectId;

    @NotEmpty(message = "Title cannot be empty")
    private String title;

    private String description;

    private Boolean isDefault;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    private LocalDateTime createdAtFrom;

    private LocalDateTime createdAtTo;

    private LocalDateTime updatedAtFrom;

    private LocalDateTime updatedAtTo;
}
