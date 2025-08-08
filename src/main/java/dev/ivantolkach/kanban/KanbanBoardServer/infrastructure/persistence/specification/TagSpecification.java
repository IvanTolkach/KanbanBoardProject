package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Tag;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TagSpecification {
    public static Specification<Tag> filterBy(TagFilterDTO filter) {
        return (Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getName() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
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
                Join<Project, User> userJoin = root.join("createdBy");
                predicates.add(cb.equal(userJoin.get("id"), filter.getCreatedBy()));
            }
            if (filter.getUpdatedBy() != null) {
                Join<Project, User> userJoin = root.join("updatedBy");
                predicates.add(cb.equal(userJoin.get("id"), filter.getUpdatedBy()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
