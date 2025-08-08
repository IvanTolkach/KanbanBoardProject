package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TaskTagRepository extends JpaRepository<TaskTag, UUID>, JpaSpecificationExecutor<TaskTag> {
    List<TaskTag> findTagsByTaskId(UUID taskId);

    List<TaskTag> findTasksByTagId(UUID tagId);

    TaskTag findByTaskIdAndTagId(UUID taskId, UUID tagId);

    boolean existsByTaskIdAndTagId(UUID taskId, UUID tagId);
}
