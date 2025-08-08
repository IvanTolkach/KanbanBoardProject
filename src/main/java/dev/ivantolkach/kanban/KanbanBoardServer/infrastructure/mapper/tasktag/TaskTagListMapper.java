package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tasktag;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.TaskTag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TaskTagMapper.class)
public interface TaskTagListMapper {
    List<TaskTagDTO> toDTOList(List<TaskTag> taskTags);

    List<TaskTag> toTaskTagList(List<TaskTagDTO> taskTagDTOs);
}
