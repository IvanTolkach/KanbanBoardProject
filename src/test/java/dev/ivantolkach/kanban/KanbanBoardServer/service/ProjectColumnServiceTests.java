package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.ProjectColumnService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.ProjectService;
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
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column.ProjectColumnListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column.ProjectColumnMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectColumnRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectColumnServiceTests {

    @InjectMocks
    private ProjectColumnService projectColumnService;

    @Mock
    private ProjectColumnRepository projectColumnRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectColumnMapper projectColumnMapper;

    @Mock
    private ProjectColumnListMapper projectColumnListMapper;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    private User makeUser(UUID id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private Project makeProject(UUID id, EntityStatus status) {
        Project p = new Project();
        p.setId(id);
        p.setStatus(status);
        return p;
    }

    private ProjectColumn makeColumn(UUID id, Project project, String title, boolean isDefault, User createdBy) {
        ProjectColumn c = new ProjectColumn();
        c.setId(id);
        c.setProject(project);
        c.setTitle(title);
        c.setDefault(isDefault);
        c.setCreatedBy(createdBy);
        return c;
    }

    @Test
    void getProjectColumnsByFilter_returnsFilteredColumns() {
        ProjectColumnFilterDTO filter = new ProjectColumnFilterDTO();
        filter.setTitle("Test Column");

        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());
        column.setTitle("Test Column");

        List<ProjectColumn> columns = List.of(column);
        List<ProjectColumnDTOOutput> expectedDTOs = List.of(new ProjectColumnDTOOutput());

        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(projectColumnRepository.findAll(any(Specification.class))).thenReturn(columns);
        when(projectColumnListMapper.toDTOList(columns)).thenReturn(expectedDTOs);

        List<ProjectColumnDTOOutput> result = projectColumnService.getProjectColumnsByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(projectColumnRepository).findAll(any(Specification.class));
        verify(projectColumnListMapper).toDTOList(columns);
    }

    @Test
    void getProjectColumnsByFilter_unauthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);

        ProjectColumnFilterDTO filter = new ProjectColumnFilterDTO();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> projectColumnService.getProjectColumnsByFilter(filter));

        assertEquals("User is not authenticated", ex.getMessage());
        verifyNoInteractions(projectColumnRepository, projectColumnListMapper);
    }

    @Test
    void getProjectColumnsByFilter_nonAdmin_addsAccessibleSpec() {
        User normal = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        when(userService.getCurrentUser()).thenReturn(normal);

        when(projectColumnRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(projectColumnListMapper.toDTOList(List.of())).thenReturn(List.of());

        projectColumnService.getProjectColumnsByFilter(new ProjectColumnFilterDTO());

        ArgumentCaptor<Specification<ProjectColumn>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(projectColumnRepository).findAll(captor.capture());
        assertNotNull(captor.getValue());
        verify(userService).getCurrentUser();
    }

    @Test
    void findDefaultProjectColumn_columnExists_returnsColumn() {
        UUID projectId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);
        ProjectColumn column = makeColumn(UUID.randomUUID(), project, "Default Column", true, null);

        when(projectColumnRepository.findOne(any(Specification.class))).thenReturn(Optional.of(column));

        ProjectColumn result = projectColumnService.findDefaultProjectColumn(projectId);

        assertEquals(column, result);
        verify(projectColumnRepository).findOne(any(Specification.class));
    }

    @Test
    void findDefaultProjectColumn_noDefaultColumn_returnsNull() {
        UUID projectId = UUID.randomUUID();
        when(projectColumnRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        ProjectColumn result = projectColumnService.findDefaultProjectColumn(projectId);

        assertNull(result);
        verify(projectColumnRepository).findOne(any(Specification.class));
    }

    @Test
    void createNewProjectColumn_noOtherColumns_setsDefault() {
        UUID projectId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("New Column");

        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());
        column.setProject(project);
        column.setTitle("New Column");

        ProjectColumnDTOOutput expectedDTO = new ProjectColumnDTOOutput();

        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of());
        when(projectColumnMapper.toProjectColumn(input)).thenReturn(column);
        when(projectColumnRepository.save(column)).thenAnswer(invocation -> {
            ProjectColumn saved = invocation.getArgument(0);
            saved.setDefault(true);
            return saved;
        });
        when(projectColumnMapper.toDTO(any(ProjectColumn.class))).thenReturn(expectedDTO);

        ProjectColumnDTOOutput result = projectColumnService.createUpdateProjectColumn(projectId, input);

        assertEquals(expectedDTO, result);

        verify(projectColumnRepository).save(column);
        verify(projectColumnMapper).toDTO(column);
    }

    @Test
    void createNewProjectColumn_emptyTitle_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);
        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("Column title cannot be empty", ex.getMessage());
        verify(projectRepository).findById(projectId);
        verifyNoInteractions(projectColumnRepository, projectColumnMapper);
    }

    @Test
    void createNewProjectColumn_projectNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("New Column");

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("Project not found with id: " + projectId, ex.getMessage());
        verify(projectRepository).findById(projectId);
        verifyNoInteractions(projectColumnRepository, projectColumnMapper);
    }

    @Test
    void createNewProjectColumn_unauthenticated_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);
        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("New Column");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("User is not authenticated", ex.getMessage());
        verify(projectRepository).findById(projectId);
        verifyNoInteractions(projectColumnRepository);
    }

    @Test
    void createNewProjectColumn_notOwnerNotAdmin_throwsUnauthorizedOnCreate() {
        UUID projectId = UUID.randomUUID();

        UUID projectCreatorId = UUID.randomUUID();
        User projectCreator = makeUser(projectCreatorId, UserRole.ROLE_CLIENT);

        Project project = makeProject(projectId, EntityStatus.ACTIVE);
        project.setCreatedBy(projectCreator);

        User currentUser = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("New Column");

        ProjectDTOOutput projectDTO = new ProjectDTOOutput();
        projectDTO.setId(projectId);
        projectDTO.setCreatedBy(projectCreatorId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        doReturn(Optional.of(projectDTO)).when(projectService).getProjectById(projectId);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("Not allowed to create this entity", ex.getMessage());

        verify(projectService).getProjectById(projectId);
        verify(projectRepository).findById(projectId);
    }


    @Test
    void updateProjectColumn_titleAndDescriptionUpdated() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);

        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Old Title");
        column.setDefault(false);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(columnId);
        input.setTitle("New Title");
        input.setDescription("New Description");

        ProjectColumnDTOOutput expectedDTO = new ProjectColumnDTOOutput();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(projectColumnRepository.save(column)).thenReturn(column);
        when(projectColumnMapper.toDTO(column)).thenReturn(expectedDTO);

        ProjectColumnDTOOutput result = projectColumnService.createUpdateProjectColumn(projectId, input);

        assertEquals(expectedDTO, result);
        assertEquals("New Title", column.getTitle());
        assertEquals("New Description", column.getDescription());
        verify(projectColumnRepository).save(column);
        verify(projectColumnMapper).toDTO(column);
    }

    @Test
    void updateProjectColumn_setAsDefault_updatesExistingDefault() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);

        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Column");
        column.setDefault(false);

        ProjectColumn existingDefault = new ProjectColumn();
        existingDefault.setId(UUID.randomUUID());
        existingDefault.setProject(project);
        existingDefault.setTitle("Default Column");
        existingDefault.setDefault(true);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(columnId);
        input.setTitle("Column");
        input.setDefault(true);

        ProjectColumnDTOOutput expectedDTO = new ProjectColumnDTOOutput();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(projectColumnRepository.findOne(any(Specification.class))).thenReturn(Optional.of(existingDefault));
        when(projectColumnRepository.save(existingDefault)).thenReturn(existingDefault);
        when(projectColumnRepository.save(column)).thenReturn(column);
        when(projectColumnMapper.toDTO(column)).thenReturn(expectedDTO);

        ProjectColumnDTOOutput result = projectColumnService.createUpdateProjectColumn(projectId, input);

        assertEquals(expectedDTO, result);
        assertTrue(column.isDefault());
        assertFalse(existingDefault.isDefault());
        verify(projectColumnRepository).save(existingDefault);
        verify(projectColumnRepository).save(column);
    }

    @Test
    void updateProjectColumn_unsetDefault_setsNewDefault() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);

        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Column");
        column.setDefault(true);

        ProjectColumn newDefault = new ProjectColumn();
        newDefault.setId(UUID.randomUUID());
        newDefault.setProject(project);
        newDefault.setTitle("Other Column");
        newDefault.setDefault(false);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(columnId);
        input.setTitle("Column");
        input.setDefault(false);

        ProjectColumnDTOOutput expectedDTO = new ProjectColumnDTOOutput();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of(newDefault));
        when(projectColumnRepository.save(newDefault)).thenReturn(newDefault);
        when(projectColumnRepository.save(column)).thenReturn(column);
        when(projectColumnMapper.toDTO(column)).thenReturn(expectedDTO);

        ProjectColumnDTOOutput result = projectColumnService.createUpdateProjectColumn(projectId, input);

        assertEquals(expectedDTO, result);
        assertFalse(column.isDefault());
        assertTrue(newDefault.isDefault());
        verify(projectColumnRepository).save(newDefault);
        verify(projectColumnRepository).save(column);
    }

    @Test
    void updateProjectColumn_unsetDefault_noOtherColumns_throwsIndexOutOfBounds() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);

        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Column");
        column.setDefault(true);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(columnId);
        input.setTitle("Column");
        input.setDefault(false);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of());

        assertThrows(IndexOutOfBoundsException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        verify(projectColumnRepository).findProjectColumnsByProjectId(projectId);
    }

    @Test
    void updateProjectColumn_columnNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID wrongId = UUID.randomUUID();
        Project project = makeProject(projectId, EntityStatus.ACTIVE);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(wrongId);
        input.setProjectId(projectId);
        input.setTitle("New Title");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(wrongId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("Column not found with id: " + input.getId(), ex.getMessage());
        verify(projectRepository).findById(projectId);
        verify(projectColumnRepository).findById(wrongId);
        verify(projectColumnRepository, never()).save(any());
    }

    @Test
    void updateProjectColumn_currentUserNull_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();

        Project project = makeProject(projectId, EntityStatus.ACTIVE);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT));

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(columnId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("User is not authenticated", ex.getMessage());
        verify(projectColumnRepository).findById(columnId);
    }

    @Test
    void updateProjectColumn_notOwnerNotAdmin_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();

        Project project = makeProject(projectId, EntityStatus.ACTIVE);
        User creator = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, creator);

        User currentUser = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(columnId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> projectColumnService.createUpdateProjectColumn(projectId, input));

        assertEquals("Not allowed to edit this entity", ex.getMessage());
        verify(projectColumnRepository).findById(columnId);
    }

    @Test
    void deleteProjectColumn_noTasks_deletesSuccessfully() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.ACTIVE);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT));

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(taskRepository.findTasksByColumnId(columnId)).thenReturn(List.of());

        projectColumnService.deleteProjectColumn(columnId);

        verify(projectColumnRepository).findById(columnId);
        verify(taskRepository).findTasksByColumnId(columnId);
        verify(projectColumnRepository).delete(column);
    }

    @Test
    void deleteProjectColumn_defaultColumnActiveProject_throwsIllegalStateException() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.ACTIVE);
        ProjectColumn column = makeColumn(columnId, project, "Default Column", true, makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT));

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Cannot delete default column in active project. Column id: " + columnId, ex.getMessage());
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_closedProject_throwsIllegalStateException() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.CLOSED);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT));

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Cannot delete columns in closed project. Column id: " + columnId, ex.getMessage());
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_withTasks_throwsIllegalStateException() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.ACTIVE);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT));
        Task task = new Task();

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(makeUser(UUID.randomUUID(), UserRole.ROLE_ADMIN));
        when(taskRepository.findTasksByColumnId(columnId)).thenReturn(List.of(task));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Cannot delete a column with tasks in it. Column id: " + columnId, ex.getMessage());
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_columnNotFound_throwsNotFoundException() {
        UUID columnId = UUID.randomUUID();
        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Column not found with id: " + columnId, ex.getMessage());
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_unauthenticated_throwsUnauthorized() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.ACTIVE);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT));

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("User is not authenticated", ex.getMessage());
        verify(projectColumnRepository).findById(columnId);
        verifyNoMoreInteractions(projectColumnRepository);
    }

    @Test
    void deleteProjectColumn_notOwnerNotAdmin_throwsUnauthorized() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.ACTIVE);
        
        User creator = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, creator);

        User currentUser = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Not allowed to delete this entity", ex.getMessage());
        verify(projectColumnRepository).findById(columnId);
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_ownerNotAdmin_deletesSuccessfully() {
        UUID columnId = UUID.randomUUID();
        Project project = makeProject(UUID.randomUUID(), EntityStatus.ACTIVE);

        User owner = makeUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        ProjectColumn column = makeColumn(columnId, project, "Column", false, owner);

        User currentUser = owner;

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(taskRepository.findTasksByColumnId(columnId)).thenReturn(List.of());

        projectColumnService.deleteProjectColumn(columnId);

        verify(projectColumnRepository).findById(columnId);
        verify(taskRepository).findTasksByColumnId(columnId);
        verify(projectColumnRepository).delete(column);
    }
}
