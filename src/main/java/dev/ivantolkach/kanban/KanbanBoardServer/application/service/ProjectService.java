package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.project.ProjectFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.project.ProjectListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.project.ProjectMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectColumnRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.ProjectRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.ProjectSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectColumnRepository projectColumnRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectListMapper projectListMapper;

    @Autowired
    TaskRepository taskRepository;

    public boolean existsById(UUID projectId) {
        return projectRepository.existsById(projectId);
    }

    public List<ProjectDTOOutput> getAllProjects() {
        return projectListMapper.toDTOList(projectRepository.findAll());
    }

    public List<ProjectDTOOutput> getProjectsByFilter(ProjectFilterDTO filter) {
        return projectListMapper.toDTOList(projectRepository.findAll(ProjectSpecification.filterBy(filter)));
    }

    public Optional<ProjectDTOOutput> getProjectById(UUID projectId) {
        return projectRepository.findById(projectId).map(projectMapper::toDTO);
    }

    public List<ProjectDTOOutput> getProjectsByCreator(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("User not found with id: " + userId));

        return projectListMapper.toDTOList(projectRepository.findByCreatedBy(user));
    }

    public List<ProjectDTOOutput> getProjectsByCreator(UUID userId, EntityStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("User not found with id: " + userId));

        return projectListMapper.toDTOList(projectRepository.findByCreatedByAndStatus(user, status));
    }

    public ProjectDTOOutput createUpdateProject(ProjectDTOInput projectDTOInput) {

        Project project;

        if (projectDTOInput.getId() != null) {
            project = projectRepository.findById(projectDTOInput.getId())
                    .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectDTOInput.getId()));

            if (project.getStatus() == EntityStatus.CLOSED && projectDTOInput.getStatus() != EntityStatus.ACTIVE) {
                throw new IllegalStateException("Cannot update closed project. Project id: " + projectDTOInput.getId());
            }

            if (!(projectDTOInput.getTitle() == null || projectDTOInput.getTitle().isBlank())) {
                project.setTitle(projectDTOInput.getTitle());
            }
            if (!(projectDTOInput.getDescription() == null || projectDTOInput.getDescription().isBlank())) {
                project.setDescription(projectDTOInput.getDescription());
            }
            if (!(projectDTOInput.getStatus() == null)) {
                if (projectDTOInput.getStatus() == EntityStatus.CLOSED) {
                    List<ProjectColumn> columns = projectColumnRepository.findProjectColumnsByProjectId(project.getId());
                    for (ProjectColumn projectColumn : columns) {
                        List<Task> tasks = taskRepository.findTasksByColumnId(projectColumn.getId());
                        for (Task task : tasks) {
                            if (task.getStatus() == EntityStatus.ACTIVE) {
                                throw new IllegalStateException("Cannot close a project if at least one task in it remains uncompleted. Project id: " + projectDTOInput.getId());
                            }
                        }
                    }
                }
                if (projectDTOInput.getStatus() == EntityStatus.RESTRICTED) {
                    throw new IllegalArgumentException("Project cant have RESTRICTED status. Project id: " + projectDTOInput.getId());
                }
                if (projectDTOInput.getStatus() == EntityStatus.CREATED) {
                    throw new IllegalArgumentException("Cannot change project status back to CREATED. Project id: " + projectDTOInput.getId());
                }
                if (projectDTOInput.getStatus() == EntityStatus.ACTIVE || projectDTOInput.getStatus() == EntityStatus.CLOSED) {
                    project.setStatus(projectDTOInput.getStatus());
                }
            }

            return projectMapper.toDTO(projectRepository.save(project));
        } else {
            if (projectDTOInput.getStatus() == EntityStatus.RESTRICTED) {
                throw new IllegalArgumentException("Project cant have RESTRICTED status.");
            }
            if (projectDTOInput.getTitle() == null || projectDTOInput.getTitle().isBlank()) {
               throw new IllegalArgumentException("Project title cannot be empty");
            }

            project = projectMapper.toProject(projectDTOInput);
            return projectMapper.toDTO(projectRepository.save(project));
        }
    }

    public void deleteProject(UUID projectId) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(()->new NotFoundException("Project not found with id: " + projectId));

        if (existingProject.getStatus() == EntityStatus.ACTIVE || existingProject.getStatus() == EntityStatus.CLOSED) {
            throw new IllegalStateException("Cannot delete active or closed project. Project id: " + projectId);
        }

        boolean hasColumns = !projectColumnRepository.findProjectColumnsByProjectId(projectId).isEmpty();

        if (hasColumns) {
            throw new IllegalStateException("Cannot delete a project with columns in it. Project id: " + projectId);
        }

        if (existingProject.getStatus() == EntityStatus.CREATED) {
            projectRepository.delete(existingProject);
            return;
        }

        throw new IllegalStateException("Cant delete project with id: " + projectId);
    }
}
