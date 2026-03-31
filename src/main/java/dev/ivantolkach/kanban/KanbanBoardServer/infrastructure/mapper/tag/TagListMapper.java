package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tag;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TagMapper.class)
public interface TagListMapper {
    List<TagDTOOutput> toDTOList(List<Tag> tag);

    List<Tag> toTagList(List<TagDTOInput> tagDTOInputs);
}
