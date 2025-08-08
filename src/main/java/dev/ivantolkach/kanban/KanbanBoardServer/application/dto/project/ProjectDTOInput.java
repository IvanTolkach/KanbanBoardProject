package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTOInput {
    private UUID id;

    private String title;

    private String description;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;
}
