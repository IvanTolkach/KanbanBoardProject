package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserTaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.UserTaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTaskService {

    @Autowired
    UserTaskRepository userTaskRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserTaskListMapper userTaskListMapper;

    public List<UserTaskDTO> getUserTasksByFilter(UserTaskFilterDTO filter) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Specification<UserTask> spec = UserTaskSpecification.filterBy(filter);

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            spec = spec.and(UserTaskSpecification.accessibleBy(currentUser));
        }

        List<UserTask> userTasks = userTaskRepository.findAll(spec);
        return userTaskListMapper.toDTOList(userTasks);
    }
}
