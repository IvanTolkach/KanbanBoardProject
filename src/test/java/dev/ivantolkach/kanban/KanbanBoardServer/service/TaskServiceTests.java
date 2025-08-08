package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.TaskService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.task.TaskListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.task.TaskMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectColumnRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserTaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.ProjectColumnService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTaskRepository userTaskRepository;

    @Mock
    private ProjectColumnRepository projectColumnRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskListMapper taskListMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserTaskMapper userTaskMapper;

    @Mock
    private ProjectColumnService projectColumnService;

    @Test
    void getTasksByFilter_returnsFilteredTasks() {
        TaskFilterDTO filter = new TaskFilterDTO();
        filter.setTitle("Test Task");
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Test Task");
        List<Task> tasks = List.of(task);
        List<TaskDTOOutput> expectedDTOs = List.of(new TaskDTOOutput());

        when(taskRepository.findAll(any(Specification.class))).thenReturn(tasks);
        when(taskListMapper.toDTOList(tasks)).thenReturn(expectedDTOs);

        List<TaskDTOOutput> result = taskService.getTasksByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(taskRepository).findAll(any(Specification.class));
        verify(taskListMapper).toDTOList(tasks);
    }

    @Test
    void getTasksByFilter_noMatches_returnsEmptyList() {
        TaskFilterDTO filter = new TaskFilterDTO();
        when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(taskListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<TaskDTOOutput> result = taskService.getTasksByFilter(filter);

        assertTrue(result.isEmpty());
        verify(taskRepository).findAll(any(Specification.class));
        verify(taskListMapper).toDTOList(List.of());
    }

    @Test
    void createNewTask_withDefaultColumn_success() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumn defaultColumn = new ProjectColumn();
        defaultColumn.setId(UUID.randomUUID());
        defaultColumn.setProject(project);
        TaskDTOInput input = new TaskDTOInput();
        input.setId(null);
        input.setTitle("New Task");
        input.setStatus(EntityStatus.ACTIVE);
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("New Task");
        task.setColumn(defaultColumn);
        task.setStatus(EntityStatus.ACTIVE);
        TaskDTOOutput expectedDTO = new TaskDTOOutput();

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(input)).thenReturn(task);
        when(projectColumnService.findDefaultProjectColumn(projectId)).thenReturn(defaultColumn);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(expectedDTO);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, input);

        assertEquals(expectedDTO, result);
        assertEquals(defaultColumn, task.getColumn());
        verify(projectRepository).existsById(projectId);
        verify(projectColumnService).findDefaultProjectColumn(projectId);
        verify(taskRepository).save(task);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void createNewTask_projectNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        TaskDTOInput input = new TaskDTOInput();
        input.setId(null);
        input.setTitle("New Task");
        input.setStatus(EntityStatus.ACTIVE);

        when(projectRepository.existsById(projectId)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Project not found with id: " + projectId, ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoInteractions(taskMapper, taskRepository, projectColumnService);
    }

    @Test
    void createNewTask_emptyTitle_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        TaskDTOInput input = new TaskDTOInput();
        input.setId(null);
        input.setTitle("");
        input.setStatus(EntityStatus.ACTIVE);

        when(projectRepository.existsById(projectId)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Task title cannot be empty", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoInteractions(taskMapper, taskRepository, projectColumnService);
    }

    @Test
    void createNewTask_invalidStatus_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        TaskDTOInput input = new TaskDTOInput();
        input.setId(null);
        input.setTitle("New Task");
        input.setStatus(EntityStatus.CREATED);

        when(projectRepository.existsById(projectId)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Task cant have CREATED or RESTRICTED status.", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoInteractions(taskMapper, taskRepository, projectColumnService);
    }

    @Test
    void updateTask_validFields_success() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Old Task");
        task.setDescription("Old Description");
        task.setStatus(EntityStatus.ACTIVE);
        TaskDTOInput input = new TaskDTOInput();
        input.setId(taskId);
        input.setTitle("New Task");
        input.setDescription("New Description");
        input.setStatus(EntityStatus.CLOSED);
        input.setColumnId(columnId);
        input.setPlannedDueDate(LocalDate.now());
        TaskDTOOutput expectedDTO = new TaskDTOOutput();

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(expectedDTO);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, input);

        assertEquals(expectedDTO, result);
        assertEquals("New Task", task.getTitle());
        assertEquals("New Description", task.getDescription());
        assertEquals(EntityStatus.CLOSED, task.getStatus());
        assertEquals(column, task.getColumn());
        assertEquals(input.getPlannedDueDate(), task.getPlannedDueDate());
        verify(taskRepository).save(task);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void updateTask_taskNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setTitle("New Project");
        project.setStatus(EntityStatus.ACTIVE);

        TaskDTOInput input = new TaskDTOInput();
        input.setId(taskId);
        input.setTitle("New Task");

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Task not found with id: " + taskId, ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verifyNoInteractions(taskMapper, projectColumnRepository, projectColumnService);
    }

    @Test
    void updateTask_closedTask_throwsIllegalStateException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.CLOSED);
        TaskDTOInput input = new TaskDTOInput();
        input.setId(taskId);
        input.setTitle("New Task");

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Cannot update closed task. Task id: " + taskId, ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper, projectColumnRepository);
    }

    @Test
    void updateTask_invalidStatus_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        TaskDTOInput input = new TaskDTOInput();
        input.setId(taskId);
        input.setStatus(EntityStatus.CREATED);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Task cant have CREATED or RESTRICTED status. Task id: " + taskId, ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper, projectColumnRepository);
    }

    @Test
    void updateTask_invalidColorParameter_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        TaskDTOInput input = new TaskDTOInput();
        input.setId(taskId);
        input.setTitle("New Task");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("color", "invalid");
        input.setParameters(parameters);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, input));

        assertEquals("Invalid color format. Use #RRGGBB or rgb(r,g,b)", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper, projectColumnRepository);
    }

    @Test
    void attachUser_newAssignment_success() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);
        UserTaskDTO userTaskDTO = new UserTaskDTO();
        UserTask userTask = new UserTask();
        userTask.setTask(task);
        userTask.setUser(user);
        userTask.setAssigned(true);
        UserTaskDTO expectedDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);
        when(userTaskRepository.save(any(UserTask.class))).thenReturn(userTask);
        when(userTaskMapper.toDTO(userTask)).thenReturn(expectedDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(expectedDTO, result);
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).save(any(UserTask.class));
        verify(userTaskMapper).toDTO(userTask);
    }

    @Test
    void attachUser_taskNotFound_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserTaskDTO userTaskDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));

        assertEquals("Task not found with id: " + taskId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_userNotFound_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        UserTaskDTO userTaskDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));

        assertEquals("User not found with id: " + userId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_closedTask_throwsIllegalStateException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.CLOSED);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);
        UserTaskDTO userTaskDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));

        assertEquals("Cannot attach user to closed task. Task id: " + taskId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_restrictedUser_throwsIllegalStateException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.RESTRICTED);
        UserTaskDTO userTaskDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));

        assertEquals("Cannot attach restricted user. User id: " + userId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_alreadyAttached_throwsIllegalStateException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);
        UserTaskDTO userTaskDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));

        assertEquals("User " + userId + " is already attached to task " + taskId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verifyNoInteractions(userTaskMapper);
    }

    @Test
    void updateUserTask_assignmentAndTimeConsumed_success() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);
        UserTask userTask = new UserTask();
        userTask.setTask(task);
        userTask.setUser(user);
        userTask.setAssigned(true);
        userTask.setTimeConsumed(10);
        UserTaskDTO userTaskDTO = new UserTaskDTO();
        userTaskDTO.setIsAssigned(false);
        userTaskDTO.setTimeConsumed(5);
        UserTaskDTO expectedDTO = new UserTaskDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);
        when(userTaskRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(userTask);
        when(userTaskRepository.save(userTask)).thenReturn(userTask);
        when(userTaskMapper.toDTO(userTask)).thenReturn(expectedDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(expectedDTO, result);
        assertFalse(userTask.isAssigned());
        assertEquals(15, userTask.getTimeConsumed());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).findByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).save(userTask);
        verify(userTaskMapper).toDTO(userTask);
    }

    @Test
    void updateUserTask_userNotAttached_throwsIllegalStateException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);
        UserTaskDTO userTaskDTO = new UserTaskDTO();
        userTaskDTO.setTimeConsumed(5);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));

        assertEquals("User " + userId + " is is not attached to a task " + taskId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verifyNoInteractions(userTaskMapper);
    }
}
