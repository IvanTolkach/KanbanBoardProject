package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
    List<Project> findByCreatedBy(User createdBy);

    List<Project> findByCreatedByAndStatus(User createdBy, EntityStatus status);
}
