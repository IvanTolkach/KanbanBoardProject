package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTOInput {
    private UUID id;

    private UUID taskId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String filePath;
}
