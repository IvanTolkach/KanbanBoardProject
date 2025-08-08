package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.ProjectColumnService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column.ProjectColumnListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column.ProjectColumnMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectColumnRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserRepository;
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
class ProjectColumnServiceTests {

    @InjectMocks
    private ProjectColumnService projectColumnService;

    @Mock
    private ProjectColumnRepository projectColumnRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectColumnMapper projectColumnMapper;

    @Mock
    private ProjectColumnListMapper projectColumnListMapper;

    @Test
    void getProjectColumnsByFilter_returnsFilteredColumns() {
        ProjectColumnFilterDTO filter = new ProjectColumnFilterDTO();
        filter.setTitle("Test Column");
        Project project = new Project();
        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());
        column.setProject(project);
        column.setTitle("Test Column");
        column.setDefault(false);
        List<ProjectColumn> columns = List.of(column);
        List<ProjectColumnDTOOutput> expectedDTOs = List.of(new ProjectColumnDTOOutput());

        when(projectColumnRepository.findAll(any(Specification.class))).thenReturn(columns);
        when(projectColumnListMapper.toDTOList(columns)).thenReturn(expectedDTOs);

        List<ProjectColumnDTOOutput> result = projectColumnService.getProjectColumnsByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(projectColumnRepository).findAll(any(Specification.class));
        verify(projectColumnListMapper).toDTOList(columns);
    }

    @Test
    void getProjectColumnsByFilter_noMatches_returnsEmptyList() {
        ProjectColumnFilterDTO filter = new ProjectColumnFilterDTO();
        when(projectColumnRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(projectColumnListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<ProjectColumnDTOOutput> result = projectColumnService.getProjectColumnsByFilter(filter);

        assertTrue(result.isEmpty());
        verify(projectColumnRepository).findAll(any(Specification.class));
        verify(projectColumnListMapper).toDTOList(List.of());
    }

    @Test
    void findDefaultProjectColumn_columnExists_returnsColumn() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());
        column.setProject(project);
        column.setTitle("Default Column");
        column.setDefault(true);

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
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("New Column");
        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());
        column.setProject(project);
        column.setTitle("New Column");
        column.setDefault(true);
        ProjectColumnDTOOutput expectedDTO = new ProjectColumnDTOOutput();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectColumnRepository.findProjectColumnsByProjectId(projectId)).thenReturn(List.of());
        when(projectColumnMapper.toProjectColumn(input)).thenReturn(column);
        when(projectColumnRepository.save(column)).thenReturn(column);
        when(projectColumnMapper.toDTO(column)).thenReturn(expectedDTO);

        ProjectColumnDTOOutput result = projectColumnService.createUpdateProjectColumn(projectId, input);

        assertEquals(expectedDTO, result);
        assertTrue(column.isDefault());
        verify(projectColumnRepository).save(column);
        verify(projectColumnMapper).toDTO(column);
    }

    @Test
    void createNewProjectColumn_emptyTitle_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumnDTOInput input = new ProjectColumnDTOInput();
        input.setId(null);
        input.setTitle("");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

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
    void updateProjectColumn_titleAndDescriptionUpdated() {
        UUID projectId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
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
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
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
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(EntityStatus.ACTIVE);
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
    void updateProjectColumn_columnNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID wrongId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setTitle("New Project");
        project.setStatus(EntityStatus.ACTIVE);

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
    void deleteProjectColumn_noTasks_deletesSuccessfully() {
        UUID columnId = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Column");
        column.setDefault(false);

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(taskRepository.findTasksByColumnId(columnId)).thenReturn(List.of());

        projectColumnService.deleteProjectColumn(columnId);

        verify(projectColumnRepository).findById(columnId);
        verify(taskRepository).findTasksByColumnId(columnId);
        verify(projectColumnRepository).delete(column);
        verifyNoMoreInteractions(projectColumnRepository, taskRepository);
    }

    @Test
    void deleteProjectColumn_defaultColumnActiveProject_throwsIllegalStateException() {
        UUID columnId = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Default Column");
        column.setDefault(true);

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Cannot delete default column in active project. Column id: " + columnId, ex.getMessage());
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_closedProject_throwsIllegalStateException() {
        UUID columnId = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setStatus(EntityStatus.CLOSED);
        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Column");
        column.setDefault(false);

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> projectColumnService.deleteProjectColumn(columnId));

        assertEquals("Cannot delete columns in closed project. Column id: " + columnId, ex.getMessage());
        verify(projectColumnRepository, never()).delete(any(ProjectColumn.class));
    }

    @Test
    void deleteProjectColumn_withTasks_throwsIllegalStateException() {
        UUID columnId = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setStatus(EntityStatus.ACTIVE);
        ProjectColumn column = new ProjectColumn();
        column.setId(columnId);
        column.setProject(project);
        column.setTitle("Column");
        column.setDefault(false);
        Task task = new Task();

        when(projectColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
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

}
