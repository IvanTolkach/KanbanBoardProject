package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Document;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = DocumentMapper.class)
public interface DocumentListMapper {
    List<DocumentDTOOutput> toDTOList(List<Document> documents);

    List<Document> toDocumentList(List<DocumentDTOInput> documentDTOInputs);
}
