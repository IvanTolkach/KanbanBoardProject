package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Document;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
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
}
