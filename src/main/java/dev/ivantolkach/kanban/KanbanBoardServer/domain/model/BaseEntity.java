package dev.ivantolkach.kanban.KanbanBoardServer.domain.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public abstract class BaseEntity {
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    @CreatedBy
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    @LastModifiedBy
    private User updatedBy;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this instanceof User user && user.getRole() == null) {
            user.setRole(UserRole.ROLE_CLIENT);
            user.setStatus(EntityStatus.CREATED);
        } else if (this instanceof Project project && project.getStatus() == null) {
            project.setStatus(EntityStatus.CREATED);
        } else if (this instanceof Task task && task.getStatus() == null) {
            task.setStatus(EntityStatus.ACTIVE);
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public abstract Object getId();
}
