package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.usertask.UserTaskFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.UserTask;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserTaskSpecification {
    public static Specification<UserTask> filterBy(UserTaskFilterDTO filter) {
        return (Root<UserTask> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getUserId() != null) {
                Join<UserTask, User> userJoin = root.join("user");
                predicates.add(cb.equal(userJoin.get("id"), filter.getUserId()));
            }
            if (filter.getTaskId() != null) {
                Join<UserTask, Task> taskJoin = root.join("task");
                predicates.add(cb.equal(taskJoin.get("id"), filter.getTaskId()));
            }
            if (filter.getTimeConsumedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeConsumed"), filter.getTimeConsumedFrom()));
            }
            if (filter.getTimeConsumedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeConsumed"), filter.getTimeConsumedTo()));
            }
            if (filter.getIsAssigned() != null) {
                predicates.add(cb.equal(root.get("isAssigned"), filter.getIsAssigned()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<UserTask> accessibleBy(User currentUser) {
        return (root, query, cb) -> {
            if (currentUser == null) {
                return cb.isTrue(cb.literal(false));
            }

            Subquery<Task> taskSubquery = query.subquery(Task.class);
            Root<Task> taskRoot = taskSubquery.from(Task.class);
            taskSubquery.select(taskRoot);
            taskSubquery.where(
                    cb.equal(taskRoot.get("id"), root.get("task").get("id")),
                    TaskSpecification.accessibleBy(currentUser).toPredicate(taskRoot, (CriteriaQuery<?>) taskSubquery.getParent(), cb)
            );

            return cb.exists(taskSubquery);
        };
    }
}
