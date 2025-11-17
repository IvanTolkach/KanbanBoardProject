package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.validator.*;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.task.TaskListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.task.TaskMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.*;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private ProjectColumnRepository projectColumnRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskListMapper taskListMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectColumnService projectColumnService;

    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private UserService userService;

    public List<TaskDTOOutput> getTasksByFilter(TaskFilterDTO filter) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Specification<Task> spec = TaskSpecification.filterBy(filter);

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            spec = spec.and(TaskSpecification.accessibleBy(currentUser));
        }

        List<Task> tasks = taskRepository.findAll(spec);
        return taskListMapper.toDTOList(tasks);
    }

    public TaskDTOOutput createUpdateTask(UUID projectId, TaskDTOInput taskDTOInput) {
        if (!projectRepository.existsById(projectId)) {
            throw new NotFoundException("Project not found with id: " + projectId);
        }

        Task task;

        if (taskDTOInput.getId() != null) {
            task = taskRepository.findById(taskDTOInput.getId())
                    .orElseThrow(()->new NotFoundException("Task not found with id: " + taskDTOInput.getId()));

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                throw new UnauthorizedException("User is not authenticated");
            }

            if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
                ProjectColumn column = task.getColumn();
                Project project = column.getProject();

                boolean isTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
                boolean isColumnAuthor = column.getCreatedBy().getId().equals(currentUser.getId());
                boolean isProjectAuthor = project.getCreatedBy().getId().equals(currentUser.getId());

                if (! (isTaskAuthor || isColumnAuthor || isProjectAuthor)) {
                    throw new UnauthorizedException("Not allowed to create or edit this entity");
                }
            }

            if (task.getStatus() == EntityStatus.CLOSED && taskDTOInput.getStatus() != EntityStatus.ACTIVE) {
                throw new IllegalStateException("Cannot update closed task. Task id: " + taskDTOInput.getId());
            }

            if (!(taskDTOInput.getTitle() == null || taskDTOInput.getTitle().isBlank())) {
                task.setTitle(taskDTOInput.getTitle());
            }
            if (!(taskDTOInput.getDescription() == null || taskDTOInput.getDescription().isBlank())) {
                task.setDescription(taskDTOInput.getDescription());
            }
            if (!(taskDTOInput.getPlannedDueDate() == null)) {
                task.setPlannedDueDate(taskDTOInput.getPlannedDueDate());
            }
            if (!(taskDTOInput.getStatus() == null)) {
                if (taskDTOInput.getStatus() == EntityStatus.CREATED || taskDTOInput.getStatus() == EntityStatus.RESTRICTED) {
                    throw new IllegalArgumentException("Task cant have CREATED or RESTRICTED status. Task id: " + taskDTOInput.getId());
                }
                if (taskDTOInput.getStatus() == EntityStatus.CLOSED || taskDTOInput.getStatus() == EntityStatus.ACTIVE) {
                task.setStatus(taskDTOInput.getStatus());
                }
            }
            if (!(taskDTOInput.getColumnId() == null)) {
                ProjectColumn existingProjectColumn = projectColumnRepository.findById(taskDTOInput.getColumnId())
                        .orElseThrow(() -> new NotFoundException("Column not found with id: " + taskDTOInput.getColumnId()));
                task.setColumn(existingProjectColumn);
            }
            if (!(taskDTOInput.getParameters() == null)) {
                task.setParameters(taskDTOInput.getParameters());
                validateParameters(task.getParameters());
            }

        } else {
            if (taskDTOInput.getStatus() == EntityStatus.CREATED || taskDTOInput.getStatus() == EntityStatus.RESTRICTED) {
                throw new IllegalArgumentException("Task cant have CREATED or RESTRICTED status.");
            }
            if (taskDTOInput.getTitle() == null || taskDTOInput.getTitle().isBlank()) {
                throw new IllegalArgumentException("Task title cannot be empty");
            }

            task = taskMapper.toTask(taskDTOInput);

            validateParameters(task.getParameters());

            ProjectColumn defaultProjectColumn = projectColumnService.findDefaultProjectColumn(projectId);
            task.setColumn(defaultProjectColumn);
        }

        return taskMapper.toDTO(taskRepository.save(task));
    }

    public UserTaskDTO attachUser(UUID taskId, UUID userId, UserTaskDTO userTaskDTO) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (task.getStatus() == EntityStatus.CLOSED) {
            throw new IllegalStateException("Cannot attach user to closed task. Task id: " + taskId);
        }
        if (user.getStatus() == EntityStatus.RESTRICTED) {
            throw new IllegalStateException("Cannot attach restricted user. User id: " + userId);
        }

        if (userTaskDTO.getTimeConsumed() == null && userTaskDTO.getIsAssigned() == null) {

            if (userTaskRepository.existsByTaskIdAndUserId(taskId, userId)) {
                throw new IllegalStateException("User " + userId + " is already attached to task " + taskId);
            }

            if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
                boolean isTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
                boolean isTaskParticipant = userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(taskId, currentUser.getId(), true);
                if (! (isTaskAuthor || isTaskParticipant)) {
                    throw new UnauthorizedException("Not allowed to create this entity");
                }
            }

            UserTask userTaskRelation = new UserTask();
            userTaskRelation.setTask(task);
            userTaskRelation.setUser(user);
            userTaskRelation.setIsAssigned(true);
            return userTaskMapper.toDTO(userTaskRepository.save(userTaskRelation));

        } else {

            if (!userTaskRepository.existsByTaskIdAndUserId(taskId, userId)) {
                throw new IllegalStateException("User " + userId + " is is not attached to a task " + taskId);
            }

            UserTask userTaskRelation = userTaskRepository.findByTaskIdAndUserId(taskId, userId);

            if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
                ProjectColumn column = task.getColumn();
                Project project = column.getProject();
                boolean isTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
                boolean isColumnAuthor = column.getCreatedBy().getId().equals(currentUser.getId());
                boolean isProjectAuthor = project.getCreatedBy().getId().equals(currentUser.getId());
                if (! (isTaskAuthor || isColumnAuthor || isProjectAuthor)) {
                    throw new UnauthorizedException("Not allowed to edit this entity");
                }
            }

            if (userTaskDTO.getIsAssigned() != null) {
                userTaskRelation.setIsAssigned(userTaskDTO.getIsAssigned());
            }
            if (userTaskDTO.getTimeConsumed() != null) {
                int updatedTimeConsumed = userTaskRelation.getTimeConsumed() + userTaskDTO.getTimeConsumed();
                userTaskRelation.setTimeConsumed(updatedTimeConsumed);
            }
            return userTaskMapper.toDTO(userTaskRepository.save(userTaskRelation));
        }
    }

    private void validateParameters(Map<String, String> parameters) {
        if (parameters == null) return;

        if (parameters.containsKey("color")) {
            String color = parameters.get("color");
            if (!ColorValidator.isValidColor(color)) {
                throw new IllegalArgumentException("Invalid color format. Use #RRGGBB or rgb(r,g,b)");
            }
        }

        if (parameters.containsKey("shape")) {
            String shape = parameters.get("shape");
            if (!ShapeValidator.isValidShape(shape)) {
                throw new IllegalArgumentException("Invalid shape. Use circle, square, triangle, or rectangle");
            }
        }

        if (parameters.containsKey("animation")) {
            String animation = parameters.get("animation");
            if (!AnimationValidator.isValidAnimation(animation)) {
                throw new IllegalArgumentException("Invalid animation. Use blink, fade, slide, or pulse");
            }
        }

        if (parameters.containsKey("size")) {
            String size = parameters.get("size");
            if (!SizeValidator.isValidSize(size)) {
                throw new IllegalArgumentException("Invalid size. Use a number between 0 and 1000");
            }
        }

        if (parameters.containsKey("opacity")) {
            String opacity = parameters.get("opacity");
            if (!OpacityValidator.isValidOpacity(opacity)) {
                throw new IllegalArgumentException("Invalid opacity. Use a number between 0.0 and 1.0");
            }
        }
    }
}
