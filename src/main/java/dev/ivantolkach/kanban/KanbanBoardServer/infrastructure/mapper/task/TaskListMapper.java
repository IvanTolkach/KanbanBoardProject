package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.task;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TaskMapper.class)
public interface TaskListMapper {
    List<TaskDTOOutput> toDTOList(List<Task> tasks);

    List<Task> toTaskList(List<TaskDTOInput> taskDTOInputs);
}
