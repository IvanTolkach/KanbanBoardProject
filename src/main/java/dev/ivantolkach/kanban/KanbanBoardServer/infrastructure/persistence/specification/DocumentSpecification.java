package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentSpecification {
    public static Specification<Document> filterBy(DocumentFilterDTO filter) {
        return (Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getTaskId() != null) {
                Join<Document, Task> taskJoin = root.join("task");
                predicates.add(cb.equal(taskJoin.get("id"), filter.getTaskId()));
            }
            if (filter.getFileName() != null) {
                predicates.add(cb.like(cb.lower(root.get("fileName")), "%" + filter.getFileName().toLowerCase() + "%"));
            }
            if (filter.getFileType() != null) {
                predicates.add(cb.like(cb.lower(root.get("fileType")), "%" + filter.getFileType().toLowerCase() + "%"));
            }
            if (filter.getFileSizeFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fileSize"), filter.getFileSizeFrom()));
            }
            if (filter.getFileSizeTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fileSize"), filter.getFileSizeTo()));
            }
            if (filter.getFilePath() != null) {
                predicates.add(cb.like(cb.lower(root.get("filePath")), "%" + filter.getFilePath().toLowerCase() + "%"));
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

    public static Specification<Document> accessibleBy(User currentUser) {
        return (root, query, cb) -> {
            if (currentUser == null) {
                return cb.isTrue(cb.literal(false));
            }

            Predicate predicate = cb.disjunction();

            Join<Document, Task> taskJoin = root.join("task");

            predicate = cb.or(predicate, cb.equal(taskJoin.get("createdBy").get("id"), currentUser.getId()));

            Subquery<UserTask> userTaskSubquery = query.subquery(UserTask.class);
            Root<UserTask> userTaskRoot = userTaskSubquery.from(UserTask.class);
            userTaskSubquery.select(userTaskRoot);
            userTaskSubquery.where(
                    cb.equal(userTaskRoot.get("task").get("id"), taskJoin.get("id")),
                    cb.equal(userTaskRoot.get("user").get("id"), currentUser.getId()),
                    cb.isTrue(userTaskRoot.get("isAssigned"))
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
