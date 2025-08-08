package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserTaskService;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.usertask.UserTaskMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserTaskRepository;
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
    private UserTaskMapper userTaskMapper;

    @Mock
    private UserTaskListMapper userTaskListMapper;

    @Test
    void getUserTasksByFilter_returnsFilteredUserTasks() {
        UserTaskFilterDTO filter = new UserTaskFilterDTO();
        filter.setUserId(UUID.randomUUID());
        UserTask userTask = new UserTask();
        userTask.setId(UUID.randomUUID());
        List<UserTask> userTasks = List.of(userTask);
        List<UserTaskDTO> expectedDTOs = List.of(new UserTaskDTO());

        when(userTaskRepository.findAll(any(Specification.class))).thenReturn(userTasks);
        when(userTaskListMapper.toDTOList(userTasks)).thenReturn(expectedDTOs);

        List<UserTaskDTO> result = userTaskService.getUserTasksByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(userTaskRepository).findAll(any(Specification.class));
        verify(userTaskListMapper).toDTOList(userTasks);
        verifyNoMoreInteractions(userTaskRepository, userTaskListMapper, userTaskMapper);
    }

    @Test
    void getUserTasksByFilter_noMatches_returnsEmptyList() {
        UserTaskFilterDTO filter = new UserTaskFilterDTO();
        filter.setUserId(UUID.randomUUID());

        when(userTaskRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(userTaskListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<UserTaskDTO> result = userTaskService.getUserTasksByFilter(filter);

        assertTrue(result.isEmpty());
        verify(userTaskRepository).findAll(any(Specification.class));
        verify(userTaskListMapper).toDTOList(List.of());
        verifyNoMoreInteractions(userTaskRepository, userTaskListMapper, userTaskMapper);
    }
}
