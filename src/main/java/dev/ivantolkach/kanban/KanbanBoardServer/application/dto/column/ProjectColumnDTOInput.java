package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectColumnDTOInput {
    private UUID id;

    private UUID projectId;

    private String title;

    private String description;

    private boolean isDefault;
}
