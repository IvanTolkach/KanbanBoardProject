package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "updatedBy.id", target = "updatedBy")
    DocumentDTOOutput toDTO(Document document);

    @Mapping(source = "taskId", target = "task.id")
    Document toDocument(DocumentDTOInput documentDTOInput);
}
