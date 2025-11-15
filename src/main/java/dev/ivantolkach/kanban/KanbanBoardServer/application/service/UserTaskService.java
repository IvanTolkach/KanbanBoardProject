package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserTaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.UserTaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserTaskService {

    @Autowired
    UserTaskRepository userTaskRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserTaskMapper userTaskMapper;

    @Autowired
    UserTaskListMapper userTaskListMapper;

    public boolean existsById(UUID userTaskId) {
        return userTaskRepository.existsById(userTaskId);
    }

    public List<UserTaskDTO> getUserTasksConnections() {
        return userTaskListMapper.toDTOList(userTaskRepository.findAll());
    }

    public Optional<UserTaskDTO> getUserTaskById(UUID userTaskId) {
        return userTaskRepository.findById(userTaskId).map(userTaskMapper::toDTO);
    }

    public List<UserTaskDTO> getUsersByTaskId(UUID taskId) {
        return userTaskListMapper.toDTOList(userTaskRepository.findUsersByTaskId(taskId));
    }

    public List<UserTaskDTO> getTasksByUserId(UUID userId) {
        return userTaskListMapper.toDTOList(userTaskRepository.findTasksByUserId(userId));
    }

    public List<UserTaskDTO> getUserTasksByFilter(UserTaskFilterDTO filter) {
        User currentUser = userService.getCurrentUser();

        Specification<UserTask> spec = UserTaskSpecification.filterBy(filter);

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            spec = spec.and(UserTaskSpecification.accessibleBy(currentUser));
        }

        List<UserTask> userTasks = userTaskRepository.findAll(spec);
        return userTaskListMapper.toDTOList(userTasks);
    }
}
