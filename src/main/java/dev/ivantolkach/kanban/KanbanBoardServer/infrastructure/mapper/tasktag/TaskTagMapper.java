package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tasktag;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.TaskTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskTagMapper {
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "tag.id", target = "tagId")
    TaskTagDTO toDTO(TaskTag taskTag);

    TaskTag toTaskTag(TaskTagDTO taskTagDTO);
}
