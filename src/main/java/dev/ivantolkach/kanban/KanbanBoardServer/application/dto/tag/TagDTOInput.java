package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTOInput {
    private UUID id;

    private String name;
}
