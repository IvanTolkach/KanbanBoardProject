package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserTaskMapper.class)
public interface UserTaskListMapper {
    List<UserTaskDTO> toDTOList(List<UserTask> userTasks);

    List<UserTask> toUserTaskList(List<UserTaskDTO> userTaskDTOs);
}
