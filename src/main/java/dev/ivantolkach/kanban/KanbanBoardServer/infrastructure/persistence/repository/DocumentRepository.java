package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Document;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {
    List<Document> findByCreatedBy(User createdBy);

    List<Document> findByTaskId(UUID taskId);
}
