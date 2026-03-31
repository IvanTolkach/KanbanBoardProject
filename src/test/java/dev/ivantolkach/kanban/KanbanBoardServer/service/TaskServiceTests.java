package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.TaskService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.ForbiddenException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
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
import org.junit.jupiter.api.BeforeEach;
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
    private ProjectColumnService projectColumnService;

    @Mock
    private UserTaskMapper userTaskMapper;

    @Mock
    private UserService userService;

    private TaskFilterDTO filter;
    private UUID projectId;
    private UUID taskId;
    private UUID userId;
    private UUID columnId;
    private User adminUser;
    private User clientUser;
    private User otherUser;
    private Project project;
    private ProjectColumn defaultColumn;
    private Task task;
    private TaskDTOInput taskDTOInput;
    private TaskDTOOutput taskDTOOutput;
    private UserTaskDTO userTaskDTO;
    private UserTask userTask;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();
        columnId = UUID.randomUUID();

        filter = new TaskFilterDTO();

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setRole(UserRole.ROLE_ADMIN);

        clientUser = new User();
        clientUser.setId(UUID.randomUUID());
        clientUser.setRole(UserRole.ROLE_CLIENT);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.ROLE_CLIENT);

        project = new Project();
        project.setId(projectId);
        project.setCreatedBy(adminUser);

        defaultColumn = new ProjectColumn();
        defaultColumn.setId(columnId);
        defaultColumn.setProject(project);
        defaultColumn.setCreatedBy(adminUser);

        task = new Task();
        task.setId(taskId);
        task.setColumn(defaultColumn);
        task.setCreatedBy(adminUser);
        task.setStatus(EntityStatus.ACTIVE);

        taskDTOInput = new TaskDTOInput();
        taskDTOInput.setTitle("Test Task");

        taskDTOOutput = new TaskDTOOutput();

        userTaskDTO = new UserTaskDTO();

        userTask = new UserTask();
        userTask.setTask(task);
        userTask.setUser(clientUser);
    }

    @Test
    void getTasksByFilter_adminUser_returnsFilteredTasks() {
        List<Task> tasks = List.of(task);
        List<TaskDTOOutput> expectedDTOs = List.of(taskDTOOutput);

        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findAll(any(Specification.class))).thenReturn(tasks);
        when(taskListMapper.toDTOList(tasks)).thenReturn(expectedDTOs);

        List<TaskDTOOutput> result = taskService.getTasksByFilter(filter);

        assertEquals(expectedDTOs, result);
        verify(userService).getCurrentUser();
        verify(taskRepository).findAll(any(Specification.class));
        verify(taskListMapper).toDTOList(tasks);
        verifyNoMoreInteractions(userService, taskRepository, taskListMapper);
    }

    @Test
    void getTasksByFilter_nonAdminUser_returnsFilteredTasks() {
        List<Task> tasks = List.of(task);
        List<TaskDTOOutput> expectedDTOs = List.of(taskDTOOutput);

        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findAll(any(Specification.class))).thenReturn(tasks);
        when(taskListMapper.toDTOList(tasks)).thenReturn(expectedDTOs);

        List<TaskDTOOutput> result = taskService.getTasksByFilter(filter);

        assertEquals(expectedDTOs, result);
        verify(userService).getCurrentUser();
        verify(taskRepository).findAll(any(Specification.class));
        verify(taskListMapper).toDTOList(tasks);
        verifyNoMoreInteractions(userService, taskRepository, taskListMapper);
    }

    @Test
    void getTasksByFilter_noMatches_returnsEmptyList() {
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(taskListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<TaskDTOOutput> result = taskService.getTasksByFilter(filter);

        assertTrue(result.isEmpty());
        verify(userService).getCurrentUser();
        verify(taskRepository).findAll(any(Specification.class));
        verify(taskListMapper).toDTOList(List.of());
        verifyNoMoreInteractions(userService, taskRepository, taskListMapper);
    }

    @Test
    void getTasksByFilter_userNotAuthenticated_throwsUnauthorizedException() {
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> taskService.getTasksByFilter(filter));
        assertEquals("User is not authenticated", ex.getMessage());
        verify(userService).getCurrentUser();
        verifyNoInteractions(taskRepository, taskListMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void createUpdateTask_create_success() {
        taskDTOInput.setId(null);
        taskDTOInput.setStatus(EntityStatus.ACTIVE);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(task);
        when(projectColumnService.findDefaultProjectColumn(projectId)).thenReturn(defaultColumn);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, taskDTOInput);

        assertEquals(taskDTOOutput, result);
        verify(projectRepository).existsById(projectId);
        verify(taskMapper).toTask(taskDTOInput);
        verify(projectColumnService).findDefaultProjectColumn(projectId);
        verify(taskRepository).save(task);
        verify(taskMapper).toDTO(task);
        verifyNoMoreInteractions(projectRepository, taskMapper, projectColumnService, taskRepository);
    }

    @Test
    void createUpdateTask_create_projectNotFound_throwsNotFoundException() {
        taskDTOInput.setId(null);
        when(projectRepository.existsById(projectId)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Project not found with id: " + projectId, ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void createUpdateTask_create_titleEmpty_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);
        taskDTOInput.setTitle("");
        when(projectRepository.existsById(projectId)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Task title cannot be empty", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void createUpdateTask_create_invalidStatusCreated_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);
        taskDTOInput.setStatus(EntityStatus.CREATED);
        when(projectRepository.existsById(projectId)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Task cant have CREATED or RESTRICTED status.", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void createUpdateTask_create_invalidStatusRestricted_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);
        taskDTOInput.setStatus(EntityStatus.RESTRICTED);
        when(projectRepository.existsById(projectId)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Task cant have CREATED or RESTRICTED status.", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void createUpdateTask_create_withValidParameters_success() {
        taskDTOInput.setId(null);
        Map<String, String> params = new HashMap<>();
        params.put("color", "#FF0000");
        params.put("shape", "circle");
        params.put("animation", "blink");
        params.put("size", "500");
        params.put("opacity", "0.5");
        taskDTOInput.setParameters(params);
        Task mockedTask = new Task();
        mockedTask.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(mockedTask);
        when(projectColumnService.findDefaultProjectColumn(projectId)).thenReturn(defaultColumn);
        when(taskRepository.save(mockedTask)).thenReturn(mockedTask);
        when(taskMapper.toDTO(mockedTask)).thenReturn(taskDTOOutput);

        taskService.createUpdateTask(projectId, taskDTOInput);

        verify(taskRepository).save(mockedTask);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void createUpdateTask_create_invalidColor_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);
        Map<String, String> params = new HashMap<>();
        params.put("color", "invalid");
        taskDTOInput.setParameters(params);
        Task mockedTask = new Task();
        mockedTask.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(mockedTask);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid color format. Use #RRGGBB or rgb(r,g,b)", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verify(taskMapper).toTask(taskDTOInput);
        verifyNoInteractions(projectColumnService);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(projectRepository, taskMapper, projectColumnService);
    }

    @Test
    void createUpdateTask_create_invalidShape_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);
        Map<String, String> params = new HashMap<>();
        params.put("shape", "invalid");
        taskDTOInput.setParameters(params);
        Task mockedTask = new Task();
        mockedTask.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(mockedTask);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid shape. Use circle, square, triangle, or rectangle", ex.getMessage());
        verify(projectRepository).existsById(projectId);
        verify(taskMapper).toTask(taskDTOInput);
        verifyNoInteractions(projectColumnService);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(projectRepository, taskMapper, projectColumnService);
    }

    @Test
    void createUpdateTask_create_invalidAnimation_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);
        Map<String, String> params = new HashMap<>();
        params.put("animation", "invalid");
        taskDTOInput.setParameters(params);

        Task mockedTask = new Task();
        mockedTask.setParameters(params);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(mockedTask);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput)
        );

        assertEquals("Invalid animation. Use blink, fade, slide, or pulse", ex.getMessage());

        verify(projectRepository).existsById(projectId);
        verify(taskMapper).toTask(taskDTOInput);
        verifyNoInteractions(projectColumnService);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(projectRepository, taskMapper);
    }

    @Test
    void createUpdateTask_create_invalidSize_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);

        Map<String, String> params = new HashMap<>();
        params.put("size", "invalid");
        taskDTOInput.setParameters(params);

        Task mockedTask = new Task();
        mockedTask.setParameters(params);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(mockedTask);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput)
        );

        assertEquals("Invalid size. Use a number between 0 and 1000", ex.getMessage());

        verify(projectRepository).existsById(projectId);
        verify(taskMapper).toTask(taskDTOInput);
        verifyNoInteractions(projectColumnService);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(projectRepository, taskMapper);
    }

    @Test
    void createUpdateTask_create_invalidOpacity_throwsIllegalArgumentException() {
        taskDTOInput.setId(null);

        Map<String, String> params = new HashMap<>();
        params.put("opacity", "invalid");
        taskDTOInput.setParameters(params);

        Task mockedTask = new Task();
        mockedTask.setParameters(params);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(mockedTask);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput)
        );

        assertEquals("Invalid opacity. Use a number between 0.0 and 1.0", ex.getMessage());

        verify(projectRepository).existsById(projectId);
        verify(taskMapper).toTask(taskDTOInput);
        verifyNoInteractions(projectColumnService);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(projectRepository, taskMapper);
    }

    @Test
    void createUpdateTask_create_nullParameters_success() {
        taskDTOInput.setId(null);
        taskDTOInput.setParameters(null);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskMapper.toTask(taskDTOInput)).thenReturn(task);
        when(projectColumnService.findDefaultProjectColumn(projectId)).thenReturn(defaultColumn);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        taskService.createUpdateTask(projectId, taskDTOInput);

        verify(taskRepository).save(task);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void createUpdateTask_update_admin_success() {
        taskDTOInput.setId(taskId);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, taskDTOInput);

        assertEquals(taskDTOOutput, result);
        verify(userService).getCurrentUser();
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_nonAdminTaskAuthor_success() {
        taskDTOInput.setId(taskId);
        task.setCreatedBy(clientUser);
        defaultColumn.setCreatedBy(otherUser);
        project.setCreatedBy(otherUser);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, taskDTOInput);

        assertEquals(taskDTOOutput, result);
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_nonAdminColumnAuthor_success() {
        taskDTOInput.setId(taskId);
        task.setCreatedBy(otherUser);
        defaultColumn.setCreatedBy(clientUser);
        project.setCreatedBy(otherUser);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, taskDTOInput);

        assertEquals(taskDTOOutput, result);
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_nonAdminProjectAuthor_success() {
        taskDTOInput.setId(taskId);
        task.setCreatedBy(otherUser);
        defaultColumn.setCreatedBy(otherUser);
        project.setCreatedBy(clientUser);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, taskDTOInput);

        assertEquals(taskDTOOutput, result);
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_setColumnPlannedDueDateAndDescription_success() {
        UUID columnId = UUID.randomUUID();

        taskDTOInput.setId(taskId);
        taskDTOInput.setColumnId(columnId);
        LocalDate dueDate = LocalDate.of(2025, 12, 31);
        taskDTOInput.setPlannedDueDate(dueDate);
        taskDTOInput.setDescription("Updated description");

        task.setTitle("Test Task");
        task.setDescription("Old description");

        ProjectColumn existingProjectColumn = new ProjectColumn();
        existingProjectColumn.setId(columnId);
        Project project = new Project();
        project.setId(projectId);
        existingProjectColumn.setProject(project);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(existingProjectColumn));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTOOutput);

        TaskDTOOutput result = taskService.createUpdateTask(projectId, taskDTOInput);

        verify(projectRepository).existsById(projectId);
        verify(userService).getCurrentUser();
        verify(taskRepository).findById(taskId);
        verify(projectColumnRepository).findById(columnId);
        verify(taskRepository).save(argThat(t ->
                t != null
                        && t.getColumn() != null
                        && columnId.equals(t.getColumn().getId())
                        && dueDate.equals(t.getPlannedDueDate())
                        && "Updated description".equals(t.getDescription())
        ));

        verify(taskMapper).toDTO(any(Task.class));
        verifyNoMoreInteractions(projectRepository, userService, taskRepository, projectColumnRepository, taskMapper);
    }

    @Test
    void createUpdateTask_update_nonAdminNotAuthor_throwsUnauthorizedException() {
        taskDTOInput.setId(taskId);
        task.setCreatedBy(otherUser);
        defaultColumn.setCreatedBy(otherUser);
        project.setCreatedBy(otherUser);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Not allowed to create or edit this entity", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_userNotAuthenticated_throwsUnauthorizedException() {
        taskDTOInput.setId(taskId);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("User is not authenticated", ex.getMessage());
        verify(userService).getCurrentUser();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void createUpdateTask_update_taskNotFound_throwsNotFoundException() {
        taskDTOInput.setId(taskId);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Task not found with id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void createUpdateTask_update_closedTaskNotToActive_throwsIllegalStateException() {
        taskDTOInput.setId(taskId);
        taskDTOInput.setStatus(EntityStatus.CLOSED);
        task.setStatus(EntityStatus.CLOSED);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Cannot update closed task. Task id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_closedTaskToActive_success() {
        taskDTOInput.setId(taskId);
        taskDTOInput.setStatus(EntityStatus.ACTIVE);
        task.setStatus(EntityStatus.CLOSED);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        taskService.createUpdateTask(projectId, taskDTOInput);

        assertEquals(EntityStatus.ACTIVE, task.getStatus());
        verify(taskRepository).save(task);
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidStatusCreated_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        taskDTOInput.setStatus(EntityStatus.CREATED);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Task cant have CREATED or RESTRICTED status. Task id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidStatusRestricted_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        taskDTOInput.setStatus(EntityStatus.RESTRICTED);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Task cant have CREATED or RESTRICTED status. Task id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_columnNotFound_throwsNotFoundException() {
        taskDTOInput.setId(taskId);
        taskDTOInput.setColumnId(columnId);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Column not found with id: " + columnId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, projectColumnRepository);
    }

    @Test
    void createUpdateTask_update_withValidParameters_success() {
        taskDTOInput.setId(taskId);
        Map<String, String> params = new HashMap<>();
        params.put("color", "#FF0000");
        taskDTOInput.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        taskService.createUpdateTask(projectId, taskDTOInput);

        verify(taskRepository).save(task);
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidColor_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        Map<String, String> params = new HashMap<>();
        params.put("color", "invalid");
        taskDTOInput.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid color format. Use #RRGGBB or rgb(r,g,b)", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidShape_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        Map<String, String> params = new HashMap<>();
        params.put("shape", "invalid");
        taskDTOInput.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid shape. Use circle, square, triangle, or rectangle", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidAnimation_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        Map<String, String> params = new HashMap<>();
        params.put("animation", "invalid");
        taskDTOInput.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid animation. Use blink, fade, slide, or pulse", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidSize_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        Map<String, String> params = new HashMap<>();
        params.put("size", "invalid");
        taskDTOInput.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid size. Use a number between 0 and 1000", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_invalidOpacity_throwsIllegalArgumentException() {
        taskDTOInput.setId(taskId);
        Map<String, String> params = new HashMap<>();
        params.put("opacity", "invalid");
        taskDTOInput.setParameters(params);
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createUpdateTask(projectId, taskDTOInput));
        assertEquals("Invalid opacity. Use a number between 0.0 and 1.0", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void createUpdateTask_update_ignoreBlankFields() {
        taskDTOInput.setId(taskId);
        taskDTOInput.setTitle("");
        taskDTOInput.setDescription(null);

        task.setTitle("Test Task");
        task.setDescription("Existing description");

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTOOutput);

        taskService.createUpdateTask(projectId, taskDTOInput);

        verify(taskRepository).save(argThat(t ->
                "Test Task".equals(t.getTitle()) &&
                        "Existing description".equals(t.getDescription())
        ));

        verify(userService).getCurrentUser();
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));

        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void attachUser_new_admin_success() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);
        when(userTaskRepository.save(any(UserTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userTaskMapper.toDTO(any(UserTask.class))).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);
        verify(userService).getCurrentUser();
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).save(any(UserTask.class));
        verify(userTaskMapper).toDTO(any(UserTask.class));
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_new_nonAdminTaskAuthor_success() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);

        task.setCreatedBy(clientUser);

        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(taskId, clientUser.getId(), true))
                .thenReturn(true);
        when(userTaskRepository.save(any(UserTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userTaskMapper.toDTO(any(UserTask.class))).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);

        verify(userService).getCurrentUser();
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).existsByTaskIdAndUserIdAndIsAssigned(taskId, clientUser.getId(), true);
        verify(userTaskRepository).save(any(UserTask.class));
        verify(userTaskMapper).toDTO(any(UserTask.class));

        verifyNoMoreInteractions(
                userService,
                taskRepository,
                userRepository,
                userTaskRepository,
                userTaskMapper
        );
    }

    @Test
    void attachUser_new_nonAdminParticipant_success() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(taskId, clientUser.getId(), true)).thenReturn(true);
        when(userTaskRepository.save(any(UserTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userTaskMapper.toDTO(any(UserTask.class))).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_new_nonAdminNotAuthorOrParticipant_throwsUnauthorizedException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(taskId, clientUser.getId(), true)).thenReturn(false);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Not allowed to create this entity", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository);
    }

    @Test
    void attachUser_new_userNotAuthenticated_throwsUnauthorizedException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("User is not authenticated", ex.getMessage());
        verify(userService).getCurrentUser();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void attachUser_new_taskNotFound_throwsNotFoundException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Task not found with id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void attachUser_new_userNotFound_throwsNotFoundException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("User not found with id: " + userId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository);
    }

    @Test
    void attachUser_new_closedTask_throwsIllegalStateException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        task.setStatus(EntityStatus.CLOSED);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Cannot attach user to closed task. Task id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository);
    }

    @Test
    void attachUser_new_restrictedUser_throwsIllegalStateException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        clientUser.setStatus(EntityStatus.RESTRICTED);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Cannot attach restricted user. User id: " + userId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository);
    }

    @Test
    void attachUser_new_alreadyAttached_throwsIllegalStateException() {
        userTaskDTO.setTimeConsumed(null);
        userTaskDTO.setIsAssigned(null);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("User " + userId + " is already attached to task " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository);
    }

    @Test
    void attachUser_update_admin_success() {
        userTaskDTO.setTimeConsumed(10);
        userTaskDTO.setIsAssigned(true);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);
        when(userTaskRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(userTask);
        when(userTaskRepository.save(userTask)).thenReturn(userTask);
        when(userTaskMapper.toDTO(userTask)).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);
        verify(userService).getCurrentUser();
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(userTaskRepository).existsByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).findByTaskIdAndUserId(taskId, userId);
        verify(userTaskRepository).save(userTask);
        verify(userTaskMapper).toDTO(userTask);
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_update_nonAdminTaskAuthor_success() {
        userTaskDTO.setTimeConsumed(10);
        task.setCreatedBy(clientUser);
        defaultColumn.setCreatedBy(otherUser);
        project.setCreatedBy(otherUser);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);
        when(userTaskRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(userTask);
        when(userTaskRepository.save(userTask)).thenReturn(userTask);
        when(userTaskMapper.toDTO(userTask)).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_update_nonAdminColumnAuthor_success() {
        userTaskDTO.setTimeConsumed(10);
        task.setCreatedBy(otherUser);
        defaultColumn.setCreatedBy(clientUser);
        project.setCreatedBy(otherUser);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);
        when(userTaskRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(userTask);
        when(userTaskRepository.save(userTask)).thenReturn(userTask);
        when(userTaskMapper.toDTO(userTask)).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_update_nonAdminProjectAuthor_success() {
        userTaskDTO.setTimeConsumed(10);
        task.setCreatedBy(otherUser);
        defaultColumn.setCreatedBy(otherUser);
        project.setCreatedBy(clientUser);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);
        when(userTaskRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(userTask);
        when(userTaskRepository.save(userTask)).thenReturn(userTask);
        when(userTaskMapper.toDTO(userTask)).thenReturn(userTaskDTO);

        UserTaskDTO result = taskService.attachUser(taskId, userId, userTaskDTO);

        assertEquals(userTaskDTO, result);
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository, userTaskMapper);
    }

    @Test
    void attachUser_update_nonAdminNotAuthor_throwsUnauthorizedException() {
        userTaskDTO.setTimeConsumed(10);
        task.setCreatedBy(otherUser);
        defaultColumn.setCreatedBy(otherUser);
        project.setCreatedBy(otherUser);
        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);
        when(userTaskRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(userTask);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Not allowed to edit this entity", ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository);
    }

    @Test
    void attachUser_update_userNotAuthenticated_throwsUnauthorizedException() {
        userTaskDTO.setTimeConsumed(10);
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("User is not authenticated", ex.getMessage());
        verify(userService).getCurrentUser();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void attachUser_update_taskNotFound_throwsNotFoundException() {
        userTaskDTO.setTimeConsumed(10);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Task not found with id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository);
    }

    @Test
    void attachUser_update_userNotFound_throwsNotFoundException() {
        userTaskDTO.setTimeConsumed(10);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("User not found with id: " + userId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository);
    }

    @Test
    void attachUser_update_closedTask_throwsIllegalStateException() {
        userTaskDTO.setTimeConsumed(10);
        task.setStatus(EntityStatus.CLOSED);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Cannot attach user to closed task. Task id: " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository);
    }

    @Test
    void attachUser_update_restrictedUser_throwsIllegalStateException() {
        userTaskDTO.setTimeConsumed(10);
        clientUser.setStatus(EntityStatus.RESTRICTED);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("Cannot attach restricted user. User id: " + userId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository);
    }

    @Test
    void attachUser_update_notAttached_throwsIllegalStateException() {
        userTaskDTO.setTimeConsumed(10);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(clientUser));
        when(userTaskRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.attachUser(taskId, userId, userTaskDTO));
        assertEquals("User " + userId + " is is not attached to a task " + taskId, ex.getMessage());
        verifyNoMoreInteractions(userService, taskRepository, userRepository, userTaskRepository);
    }
}