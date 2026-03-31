package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface UserTaskRepository extends JpaRepository<UserTask, UUID>, JpaSpecificationExecutor<UserTask> {
    List<UserTask> findUsersByTaskId(UUID taskId);

    List<UserTask> findTasksByUserId(UUID userId);

    UserTask findByTaskIdAndUserId(UUID taskId, UUID userId);

    boolean existsByTaskIdAndUserId(UUID taskId, UUID userId);

    boolean existsByTaskIdAndUserIdAndIsAssigned(UUID taskId, UUID userId, boolean assigned);
}
