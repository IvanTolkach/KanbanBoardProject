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

    public static Specification<TaskTag> accessibleBy(User currentUser) {
        return (root, query, cb) -> {
            if (currentUser == null) {
                return cb.isTrue(cb.literal(false));
            }

            Predicate predicate = cb.disjunction();

            Join<TaskTag, Task> taskJoin = root.join("task");

            predicate = cb.or(predicate, cb.equal(taskJoin.get("createdBy").get("id"), currentUser.getId()));

            Subquery<UserTask> userTaskSubquery = query.subquery(UserTask.class);
            Root<UserTask> userTaskRoot = userTaskSubquery.from(UserTask.class);
            userTaskSubquery.select(userTaskRoot);
            userTaskSubquery.where(
                    cb.equal(userTaskRoot.get("task").get("id"), taskJoin.get("id")),
                    cb.equal(userTaskRoot.get("user").get("id"), currentUser.getId())
            );
            predicate = cb.or(predicate, cb.exists(userTaskSubquery));

            Join<Task, ProjectColumn> columnJoin = taskJoin.join("column");
            predicate = cb.or(predicate, cb.equal(columnJoin.get("createdBy").get("id"), currentUser.getId()));

            Join<ProjectColumn, Project> projectJoin = columnJoin.join("project");
            predicate = cb.or(predicate, cb.equal(projectJoin.get("createdBy").get("id"), currentUser.getId()));

            return predicate;
        };
    }
}
