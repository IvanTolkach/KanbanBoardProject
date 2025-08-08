package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskTagSpecification {
    public static Specification<TaskTag> filterBy(TaskTagDTO filter) {
        return (Root<TaskTag> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getTaskId() != null) {
                Join<TaskTag, Task> taskJoin = root.join("task");
                predicates.add(cb.equal(taskJoin.get("id"), filter.getTaskId()));
            }
            if (filter.getTagId() != null) {
                Join<TaskTag, Tag> tagJoin = root.join("tag");
                predicates.add(cb.equal(tagJoin.get("id"), filter.getTagId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
