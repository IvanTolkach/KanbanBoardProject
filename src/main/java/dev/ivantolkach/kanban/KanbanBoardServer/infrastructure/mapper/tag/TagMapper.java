package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tag;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "updatedBy.id", target = "updatedBy")
    TagDTOOutput toDTO(Tag tag);

    Tag toTag(TagDTOInput tagDTOInput);
}
