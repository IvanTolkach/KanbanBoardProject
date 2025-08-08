package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserTaskMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "assigned", target = "isAssigned")
    UserTaskDTO toDTO(UserTask userTask);

    UserTask toUserTask(UserTaskDTO userTaskDTO);
}
