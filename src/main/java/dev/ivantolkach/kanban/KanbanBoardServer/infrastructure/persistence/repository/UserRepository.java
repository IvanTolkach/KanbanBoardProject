package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);
}
