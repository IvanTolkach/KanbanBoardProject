package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.column.ProjectColumnFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectColumnSpecification {
    public static Specification<ProjectColumn> findDefaultByProjectId(UUID projectId) {
        return (root, query, cb) -> {
            Join<ProjectColumn, Project> projectJoin = root.join("project");
            return cb.and(
                    cb.equal(projectJoin.get("id"), projectId),
                    cb.isTrue(root.get("isDefault"))
            );
        };
    }

    public static Specification<ProjectColumn> filterBy(ProjectColumnFilterDTO filter) {
        return (Root<ProjectColumn> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getTitle() != null) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));
            }
            if (filter.getDescription() != null) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().toLowerCase() + "%"));
            }
            if (filter.getProjectId() != null) {
                Join<ProjectColumn, Project> projectJoin = root.join("project");
                predicates.add(cb.equal(projectJoin.get("id"), filter.getProjectId()));
            }
            if (filter.getIsDefault() != null) {
                predicates.add(cb.equal(root.get("isDefault"), filter.getIsDefault()));
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
                Join<ProjectColumn, User> userJoin = root.join("createdBy");
                predicates.add(cb.equal(userJoin.get("id"), filter.getCreatedBy()));
            }
            if (filter.getUpdatedBy() != null) {
                Join<ProjectColumn, User> userJoin = root.join("updatedBy");
                predicates.add(cb.equal(userJoin.get("id"), filter.getUpdatedBy()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ProjectColumn> accessibleBy(User currentUser) {
        return (root, query, cb) -> {
            if (currentUser == null) {
                return cb.isTrue(cb.literal(false));
            }

            Predicate predicate = cb.disjunction();

            Join<ProjectColumn, Project> projectJoin = root.join("project");
            predicate = cb.or(predicate, cb.equal(projectJoin.get("createdBy").get("id"), currentUser.getId()));

            predicate = cb.or(predicate, cb.equal(root.get("createdBy").get("id"), currentUser.getId()));

            Subquery<Task> taskAuthorSubquery = query.subquery(Task.class);
            Root<Task> taskRoot = taskAuthorSubquery.from(Task.class);
            taskAuthorSubquery.select(taskRoot);
            taskAuthorSubquery.where(
                    cb.equal(taskRoot.get("column").get("id"), root.get("id")),
                    cb.equal(taskRoot.get("createdBy").get("id"), currentUser.getId())
            );
            predicate = cb.or(predicate, cb.exists(taskAuthorSubquery));

            Subquery<UserTask> userTaskSubquery = query.subquery(UserTask.class);
            Root<UserTask> userTaskRoot = userTaskSubquery.from(UserTask.class);
            userTaskSubquery.select(userTaskRoot);
            userTaskSubquery.where(
                    cb.equal(userTaskRoot.get("task").get("column").get("id"), root.get("id")),
                    cb.equal(userTaskRoot.get("user").get("id"), currentUser.getId()),
                    cb.isTrue(userTaskRoot.get("isAssigned"))
            );
            predicate = cb.or(predicate, cb.exists(userTaskSubquery));

            return predicate;
        };
    }
}
