package dev.ivantolkach.kanban.KanbanBoardServer.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "User_Task", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "task_id"}))
public class UserTask {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private Task task;

    @Column(name = "time_consumed")
    private int timeConsumed;

    @Column(name = "is_assigned", columnDefinition = "boolean default true", nullable = false)
    private boolean isAssigned = true;
}
