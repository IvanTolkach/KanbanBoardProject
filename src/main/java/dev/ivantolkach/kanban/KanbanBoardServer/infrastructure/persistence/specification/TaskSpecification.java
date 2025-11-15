package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.task.TaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {
    public static Specification<Task> filterBy(TaskFilterDTO filter) {
        return (Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getColumnId() != null) {
                Join<Task, ProjectColumn> projectColumnJoin = root.join("column");
                predicates.add(cb.equal(projectColumnJoin.get("id"), filter.getColumnId()));
            }
            if (filter.getTitle() != null) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));
            }
            if (filter.getDescription() != null) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().toLowerCase() + "%"));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getPlannedDueDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("plannedDueDate"), filter.getPlannedDueDateFrom()));
            }
            if (filter.getPlannedDueDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("plannedDueDate"), filter.getPlannedDueDateTo()));
            }
            if (filter.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));
            }
            if (filter.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));
            }
            if (filter.getUpdatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAtFrom()));
            }
            if (filter.getUpdatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAtTo()));
            }
            if (filter.getCreatedBy() != null) {
                Join<Task, User> userJoin = root.join("createdBy");
                predicates.add(cb.equal(userJoin.get("id"), filter.getCreatedBy()));
            }
            if (filter.getUpdatedBy() != null) {
                Join<Task, User> userJoin = root.join("updatedBy");
                predicates.add(cb.equal(userJoin.get("id"), filter.getUpdatedBy()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Task> accessibleBy(User currentUser) {
        return (root, query, cb) -> {
            if (currentUser == null) {
                return cb.isTrue(cb.literal(false));
            }

            Predicate predicate = cb.disjunction();

            Join<Task, ProjectColumn> columnJoin = root.join("column");
            Join<ProjectColumn, Project> projectJoin = columnJoin.join("project");
            predicate = cb.or(predicate, cb.equal(projectJoin.get("createdBy").get("id"), currentUser.getId()));

            predicate = cb.or(predicate, cb.equal(columnJoin.get("createdBy").get("id"), currentUser.getId()));

            Subquery<UserTask> userTaskSubquery = query.subquery(UserTask.class);
            Root<UserTask> userTaskRoot = userTaskSubquery.from(UserTask.class);
            userTaskSubquery.select(userTaskRoot);
            userTaskSubquery.where(
                    cb.equal(userTaskRoot.get("task").get("id"), root.get("id")),
                    cb.equal(userTaskRoot.get("user").get("id"), currentUser.getId()),
                    cb.isTrue(userTaskRoot.get("isAssigned"))
            );
            predicate = cb.or(predicate, cb.exists(userTaskSubquery));

            return predicate;
        };
    }
}
