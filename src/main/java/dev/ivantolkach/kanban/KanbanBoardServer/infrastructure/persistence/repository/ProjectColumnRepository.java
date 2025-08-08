package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.ProjectColumn;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ProjectColumnRepository extends JpaRepository<ProjectColumn, UUID>, JpaSpecificationExecutor<ProjectColumn> {
    List<ProjectColumn> findByCreatedBy(User createdBy);

    List<ProjectColumn> findProjectColumnsByProjectId(UUID projectId);
}
