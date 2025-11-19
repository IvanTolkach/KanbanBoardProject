package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserTaskService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTaskServiceTests {

    @InjectMocks
    private UserTaskService userTaskService;

    @Mock
    private UserTaskRepository userTaskRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserTaskListMapper userTaskListMapper;

    private UserTaskFilterDTO filter;
    private User adminUser;
    private User clientUser;

    @BeforeEach
    void setUp() {
        filter = new UserTaskFilterDTO();
        filter.setUserId(UUID.randomUUID());

        adminUser = new User();
        adminUser.setRole(UserRole.ROLE_ADMIN);

        clientUser = new User();
        clientUser.setRole(UserRole.ROLE_CLIENT);
    }

    @Test
    void getUserTasksByFilter_adminUser_returnsFilteredUserTasks() {
        UserTask userTask = new UserTask();
        userTask.setId(UUID.randomUUID());
        List<UserTask> userTasks = List.of(userTask);
        List<UserTaskDTO> expectedDTOs = List.of(new UserTaskDTO());

        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userTaskRepository.findAll(any(Specification.class))).thenReturn(userTasks);
        when(userTaskListMapper.toDTOList(userTasks)).thenReturn(expectedDTOs);

        List<UserTaskDTO> result = userTaskService.getUserTasksByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());

        verify(userService).getCurrentUser();
        verify(userTaskRepository).findAll(any(Specification.class));
        verify(userTaskListMapper).toDTOList(userTasks);
        verifyNoMoreInteractions(userService, userTaskRepository, userTaskListMapper);
    }

    @Test
    void getUserTasksByFilter_nonAdminUser_returnsFilteredUserTasks() {
        UserTask userTask = new UserTask();
        userTask.setId(UUID.randomUUID());
        List<UserTask> userTasks = List.of(userTask);
        List<UserTaskDTO> expectedDTOs = List.of(new UserTaskDTO());

        when(userService.getCurrentUser()).thenReturn(clientUser);
        when(userTaskRepository.findAll(any(Specification.class))).thenReturn(userTasks);
        when(userTaskListMapper.toDTOList(userTasks)).thenReturn(expectedDTOs);

        List<UserTaskDTO> result = userTaskService.getUserTasksByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());

        verify(userService).getCurrentUser();
        verify(userTaskRepository).findAll(any(Specification.class));
        verify(userTaskListMapper).toDTOList(userTasks);
        verifyNoMoreInteractions(userService, userTaskRepository, userTaskListMapper);
    }

    @Test
    void getUserTasksByFilter_noMatches_returnsEmptyList() {
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userTaskRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(userTaskListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<UserTaskDTO> result = userTaskService.getUserTasksByFilter(filter);

        assertTrue(result.isEmpty());

        verify(userService).getCurrentUser();
        verify(userTaskRepository).findAll(any(Specification.class));
        verify(userTaskListMapper).toDTOList(List.of());
        verifyNoMoreInteractions(userService, userTaskRepository, userTaskListMapper);
    }

    @Test
    void getUserTasksByFilter_userNotAuthenticated_throwsUnauthorizedException() {
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> userTaskService.getUserTasksByFilter(filter));
        assertEquals("User is not authenticated", exception.getMessage());

        verify(userService).getCurrentUser();
        verifyNoInteractions(userTaskRepository, userTaskListMapper);
    }
}
