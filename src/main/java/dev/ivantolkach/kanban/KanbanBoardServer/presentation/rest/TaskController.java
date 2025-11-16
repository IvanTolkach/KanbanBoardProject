package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.TaskService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserTaskService;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.TaskEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TaskController implements TaskEndpoint {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserTaskService userTaskService;

    @Override
    public List<TaskDTOOutput> getTasks(TaskFilterDTO filter) {
        return taskService.getTasksByFilter(filter);
    }

    @Override
    public List<UserTaskDTO> getUsersTasks(UserTaskFilterDTO filter) {
        return userTaskService.getUserTasksByFilter(filter);
    }

    @Override
    public TaskDTOOutput createUpdateTask(UUID projectId, TaskDTOInput task) {
        return taskService.createUpdateTask(projectId, task);
    }

    @Override
    public UserTaskDTO attachUser(UUID taskId, UUID userId, UserTaskDTO userTaskDTO) {
        return taskService.attachUser(taskId, userId, userTaskDTO);
    }
}
