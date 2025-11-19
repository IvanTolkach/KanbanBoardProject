package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectFilterDTO;
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
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.project.ProjectListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.project.ProjectMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectColumnRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ProjectServiceTests {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectColumnRepository projectColumnRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectListMapper projectListMapper;

    @Mock
    private UserService userService;

    private User createUser(UUID id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    @Test
    void getProjectsByFilter_returnsFilteredProjects() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setTitle("Test Project");

        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setTitle("Test Project");

        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setTitle("Test Project");

        List<Project> filteredProjects = List.of(project1, project2);

        ProjectDTOOutput dto1 = new ProjectDTOOutput();
        ProjectDTOOutput dto2 = new ProjectDTOOutput();
        List<ProjectDTOOutput> expectedDTOs = List.of(dto1, dto2);

        when(projectRepository.findAll(any(Specification.class))).thenReturn(filteredProjects);
        when(projectListMapper.toDTOList(filteredProjects)).thenReturn(expectedDTOs);

        List<ProjectDTOOutput> result = projectService.getProjectsByFilter(filter);

        assertEquals(2, result.size());
        assertEquals(expectedDTOs, result);

        verify(projectRepository).findAll(any(Specification.class));
        verify(projectListMapper).toDTOList(filteredProjects);
    }

    @Test
    void getProjectsByFilter_returnsEmptyList() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        when(projectRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(projectListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<ProjectDTOOutput> result = projectService.getProjectsByFilter(filter);

        assertTrue(result.isEmpty());
        verify(projectRepository).findAll(any(Specification.class));
        verify(projectListMapper).toDTOList(List.of());
    }

    @Test
    void getProjectById_returnsDtoWhenFound() {
        UUID projectId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setTitle("My Project");

        ProjectDTOOutput dto = new ProjectDTOOutput();
        dto.setTitle("My Project");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toDTO(project)).thenReturn(dto);

        Optional<ProjectDTOOutput> result = projectService.getProjectById(projectId);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());

        verify(projectRepository).findById(projectId);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void getProjectById_returnsEmptyWhenNotFound() {
        UUID projectId = UUID.randomUUID();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        Optional<ProjectDTOOutput> result = projectService.getProjectById(projectId);

        assertTrue(result.isEmpty());

        verify(projectRepository).findById(projectId);
        verify(projectMapper, never()).toDTO(any());
    }

    @Test
    void getProjectById_returnsEmptyIfMapperReturnsNull() {
        UUID projectId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toDTO(project)).thenReturn(null);

        Optional<ProjectDTOOutput> result = projectService.getProjectById(projectId);

        assertTrue(result.isEmpty());

        verify(projectRepository).findById(projectId);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void createNewProject_successfully() {
        ProjectDTOInput input = new ProjectDTOInput();
        input.setTitle("Test project");
        input.setDescription("Test description");

        Project project = new Project();
        project.setTitle(input.getTitle());
        project.setDescription(input.getDescription());

        ProjectDTOOutput expectedDTO = new ProjectDTOOutput();

        when(projectMapper.toProject(input)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDTO(project)).thenReturn(expectedDTO);

        ProjectDTOOutput result = projectService.createUpdateProject(input);

        assertEquals(expectedDTO, result);
        verify(projectMapper).toProject(input);
        verify(projectRepository).save(project);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void createNewProject_withRestrictedStatus_throwsException() {
        ProjectDTOInput input = new ProjectDTOInput();
        input.setTitle("Test");
        input.setStatus(EntityStatus.RESTRICTED);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.createUpdateProject(input));

        assertTrue(exception.getMessage().toLowerCase().contains("restricted"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createNewProject_withNullTitle_throwsException() {
        ProjectDTOInput input = new ProjectDTOInput();
        input.setTitle(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectService.createUpdateProject(input));

        assertTrue(ex.getMessage().toLowerCase().contains("cannot be empty"));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void createNewProject_withBlankTitle_throwsException() {
        ProjectDTOInput input = new ProjectDTOInput();
        input.setTitle("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectService.createUpdateProject(input));

        assertTrue(ex.getMessage().toLowerCase().contains("cannot be empty"));
        verify(projectRepository, never()).save(any(Project.class));
    }


    @Test
    void updateProject_nonExistentProject_throwsException() {
        UUID projectId = UUID.randomUUID();
        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setTitle("Updated Title");
        input.setStatus(EntityStatus.ACTIVE);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> projectService.createUpdateProject(input));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_withoutAuthentication_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();
        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setTitle("Updated Title");

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setStatus(EntityStatus.ACTIVE);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userService.getCurrentUser()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> projectService.createUpdateProject(input));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_permissionDenied_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();
        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setTitle("Updated Title");

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        User other = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setStatus(EntityStatus.ACTIVE);
        existingProject.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userService.getCurrentUser()).thenReturn(other);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> projectService.createUpdateProject(input));
        assertTrue(ex.getMessage().toLowerCase().contains("not allowed"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_titleAndDescriptionUpdatedSuccessfully() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setTitle("Updated Title");
        input.setDescription("Updated Description");

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        project.setCreatedBy(owner);

        ProjectDTOOutput expected = new ProjectDTOOutput();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(owner);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDTO(project)).thenReturn(expected);

        ProjectDTOOutput result = projectService.createUpdateProject(input);

        assertEquals(expected, result);
        assertEquals("Updated Title", project.getTitle());
        assertEquals("Updated Description", project.getDescription());

        verify(projectRepository).save(project);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void updateClosedProject_withoutChangingStatusToActive_throwsException() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setTitle("New title");

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.CLOSED);
        project.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(owner);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectService.createUpdateProject(input));

        assertTrue(ex.getMessage().toLowerCase().contains("cannot update closed project"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_withStatusSetToCreated_throwsException() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setStatus(EntityStatus.CREATED);

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setStatus(EntityStatus.ACTIVE);
        existingProject.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userService.getCurrentUser()).thenReturn(owner);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectService.createUpdateProject(input));

        assertTrue(ex.getMessage().toLowerCase().contains("cannot change project status back to created"));
    }

    @Test
    void updateProject_setStatusToRestricted_throwsException() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setStatus(EntityStatus.RESTRICTED);

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setStatus(EntityStatus.ACTIVE);
        existingProject.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userService.getCurrentUser()).thenReturn(owner);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectService.createUpdateProject(input));

        assertTrue(ex.getMessage().toLowerCase().contains("restricted"));
    }

    @Test
    void updateProject_closeProjectFailsIfActiveTasksExist() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setStatus(EntityStatus.CLOSED);

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setStatus(EntityStatus.ACTIVE);
        existingProject.setCreatedBy(owner);

        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());

        Task activeTask = new Task();
        activeTask.setStatus(EntityStatus.ACTIVE);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userService.getCurrentUser()).thenReturn(owner);
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of(column));
        when(taskRepository.findTasksByColumnId(column.getId())).thenReturn(List.of(activeTask));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectService.createUpdateProject(input));
        assertTrue(ex.getMessage().toLowerCase().contains("uncompleted"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_closeProjectSucceedsWhenNoActiveTasks() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setStatus(EntityStatus.CLOSED);

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setStatus(EntityStatus.ACTIVE);
        existingProject.setCreatedBy(owner);

        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());

        Task doneTask = new Task();
        doneTask.setStatus(EntityStatus.CLOSED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userService.getCurrentUser()).thenReturn(owner);
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of(column));
        when(taskRepository.findTasksByColumnId(column.getId())).thenReturn(List.of(doneTask));
        when(projectRepository.save(existingProject)).thenReturn(existingProject);
        ProjectDTOOutput out = new ProjectDTOOutput();
        when(projectMapper.toDTO(existingProject)).thenReturn(out);

        ProjectDTOOutput result = projectService.createUpdateProject(input);

        assertEquals(out, result);
        assertEquals(EntityStatus.CLOSED, existingProject.getStatus());
        verify(projectRepository).save(existingProject);
    }

    @Test
    void deleteProject_withCreatedStatusAndNoColumns_deletesSuccessfully() {
        UUID projectId = UUID.randomUUID();

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.CREATED);
        project.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(owner);
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of());

        assertDoesNotThrow(() -> projectService.deleteProject(projectId));
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_notAuthenticated_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.CREATED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> projectService.deleteProject(projectId));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void deleteProject_permissionDenied_throwsUnauthorized() {
        UUID projectId = UUID.randomUUID();

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        User other = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.CREATED);
        project.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(other);

        assertThrows(ForbiddenException.class, () -> projectService.deleteProject(projectId));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void deleteProject_withActiveStatus_throwsException() {
        UUID projectId = UUID.randomUUID();

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        project.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(owner);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectService.deleteProject(projectId));

        assertTrue(ex.getMessage().toLowerCase().contains("cannot delete active or closed project"));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void deleteProject_withColumns_throwsException() {
        UUID projectId = UUID.randomUUID();

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.CREATED);
        project.setCreatedBy(owner);

        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(owner);
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of(column));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectService.deleteProject(projectId));

        assertTrue(ex.getMessage().toLowerCase().contains("cannot delete a project with columns in it"));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void updateProject_asAdmin_allowedEvenIfNotCreator() {
        UUID projectId = UUID.randomUUID();

        ProjectDTOInput input = new ProjectDTOInput();
        input.setId(projectId);
        input.setTitle("Admin updated title");

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);
        User admin = createUser(UUID.randomUUID(), UserRole.ROLE_ADMIN);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        project.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(admin);
        when(projectRepository.save(project)).thenReturn(project);
        ProjectDTOOutput dto = new ProjectDTOOutput();
        when(projectMapper.toDTO(project)).thenReturn(dto);

        ProjectDTOOutput result = projectService.createUpdateProject(input);

        assertEquals(dto, result);
        assertEquals("Admin updated title", project.getTitle());
        verify(projectRepository).save(project);
    }

    @Test
    void deleteProject_withInvalidStatus_throwsException() {
        UUID projectId = UUID.randomUUID();

        User owner = createUser(UUID.randomUUID(), UserRole.ROLE_CLIENT);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.RESTRICTED);
        project.setCreatedBy(owner);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(owner);
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectService.deleteProject(projectId));

        assertTrue(ex.getMessage().toLowerCase().contains("cant delete project"));
        verify(projectRepository, never()).delete(any(Project.class));
    }
}