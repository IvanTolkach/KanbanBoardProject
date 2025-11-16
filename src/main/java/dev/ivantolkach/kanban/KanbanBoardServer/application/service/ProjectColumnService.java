package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column.ProjectColumnListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.column.ProjectColumnMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectColumnRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.ProjectColumnSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectColumnService {

    @Autowired
    ProjectColumnRepository projectColumnRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectColumnMapper projectColumnMapper;

    @Autowired
    ProjectColumnListMapper projectColumnListMapper;

    @Autowired
    UserService userService;

    @Autowired
    ProjectService projectService;

    public boolean existsById(UUID columnId) {
        return projectColumnRepository.existsById(columnId);
    }

    public List<ProjectColumnDTOOutput> getAllProjectColumns() {
        return projectColumnListMapper.toDTOList(projectColumnRepository.findAll());
    }

    public List<ProjectColumnDTOOutput> getProjectColumnsByFilter(ProjectColumnFilterDTO filter) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Specification<ProjectColumn> spec = ProjectColumnSpecification.filterBy(filter);

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            spec = spec.and(ProjectColumnSpecification.accessibleBy(currentUser));
        }

        List<ProjectColumn> projectColumns = projectColumnRepository.findAll(spec);
        return projectColumnListMapper.toDTOList(projectColumns);
    }

    public Optional<ProjectColumnDTOOutput> getProjectColumnById(UUID columnId) {
        return projectColumnRepository.findById(columnId).map(projectColumnMapper::toDTO);
    }

    public List<ProjectColumnDTOOutput> getProjectColumnsByProjectId(UUID projectId) {
        return projectColumnListMapper.toDTOList(projectColumnRepository.findProjectColumnsByProjectId(projectId));
    }

    public List<ProjectColumnDTOOutput> getProjectColumnsByCreator(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return projectColumnListMapper.toDTOList(projectColumnRepository.findByCreatedBy(user));
    }

    public ProjectColumn findDefaultProjectColumn(UUID projectId) {
        return projectColumnRepository.findOne(ProjectColumnSpecification.findDefaultByProjectId(projectId))
                .orElse(null);
    }

    public ProjectColumnDTOOutput createUpdateProjectColumn(UUID projectId, ProjectColumnDTOInput projectColumnDTOInput) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

        ProjectColumn projectColumn;

        if (projectColumnDTOInput.getId() != null) {
            projectColumn = projectColumnRepository.findById(projectColumnDTOInput.getId())
                    .orElseThrow(() -> new NotFoundException("Column not found with id: " + projectColumnDTOInput.getId()));

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                throw new UnauthorizedException("User is not authenticated");
            }

            if (!(currentUser.getRole() == UserRole.ROLE_ADMIN)) {
                if (!(projectColumn.getCreatedBy().getId().equals(currentUser.getId()))) {
                    throw new UnauthorizedException("Not allowed to edit this entity");
                }
            }

            if (!(projectColumnDTOInput.getTitle() == null || projectColumnDTOInput.getTitle().isBlank())) {
                projectColumn.setTitle(projectColumnDTOInput.getTitle());
            }
            if (!(projectColumnDTOInput.getDescription() == null || projectColumnDTOInput.getDescription().isBlank())) {
                projectColumn.setDescription(projectColumnDTOInput.getDescription());
            }

            Boolean isDefault = projectColumnDTOInput.isDefault();
            if (isDefault != null) {
                if (isDefault) {
                    ProjectColumn defaultProjectColumn = findDefaultProjectColumn(projectColumn.getProject().getId());
                    if (defaultProjectColumn != null) {
                        defaultProjectColumn.setDefault(false);
                        projectColumnRepository.save(defaultProjectColumn);
                    }
                    projectColumn.setDefault(true);
                } else if (projectColumn.isDefault()) {
                    ProjectColumn newDefaultProjectColumn = projectColumnRepository.findProjectColumnsByProjectId(projectColumn.getProject().getId()).get(0);
                    newDefaultProjectColumn.setDefault(true);
                    projectColumnRepository.save(newDefaultProjectColumn);
                    projectColumn.setDefault(false);
                }
            }

        } else {

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                throw new UnauthorizedException("User is not authenticated");
            }

            if (!(currentUser.getRole() == UserRole.ROLE_ADMIN)) {
                if (!(projectService.getProjectById(projectId).get().getCreatedBy().equals(currentUser.getId()))) {
                    throw new UnauthorizedException("Not allowed to create this entity");
                }
            }

            if (projectColumnDTOInput.getTitle() == null || projectColumnDTOInput.getTitle().isBlank()) {
                throw new IllegalArgumentException("Column title cannot be empty");
            }

            projectColumn = projectColumnMapper.toProjectColumn(projectColumnDTOInput);
            projectColumn.setProject(project);

            if (projectColumnRepository.findProjectColumnsByProjectId(projectId).isEmpty()) {
                projectColumn.setDefault(true);
            }
        }

        return projectColumnMapper.toDTO(projectColumnRepository.save(projectColumn));
    }


    public void deleteProjectColumn(UUID columnId) {
        ProjectColumn existingProjectColumn = projectColumnRepository.findById(columnId)
                .orElseThrow(()->new NotFoundException("Column not found with id: " + columnId));

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (!(currentUser.getRole() == UserRole.ROLE_ADMIN)) {
            if (!(existingProjectColumn.getCreatedBy().getId().equals(currentUser.getId()))) {
                throw new UnauthorizedException("Not allowed to delete this entity");
            }
        }

        if (existingProjectColumn.getProject().getStatus() == EntityStatus.ACTIVE && existingProjectColumn.isDefault()) {
            throw new IllegalStateException("Cannot delete default column in active project. Column id: " + columnId);
        }

        if (existingProjectColumn.getProject().getStatus() == EntityStatus.CLOSED) {
            throw new IllegalStateException("Cannot delete columns in closed project. Column id: " + columnId);
        }

        boolean hasTasks = !taskRepository.findTasksByColumnId(columnId).isEmpty();

        if (hasTasks) {
            throw new IllegalStateException("Cannot delete a column with tasks in it. Column id: " + columnId);
        }

        projectColumnRepository.delete(existingProjectColumn);
    }
}
