package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tag.TagListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tag.TagMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tasktag.TaskTagListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.tasktag.TaskTagMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TagRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskTagRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.TagSpecification;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.TaskTagSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    TaskTagRepository taskTagRepository;

    @Autowired
    TaskTagMapper taskTagMapper;

    @Autowired
    TaskTagListMapper taskTagListMapper;

    @Autowired
    TagMapper tagMapper;

    @Autowired
    TagListMapper tagListMapper;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserService userService;

    public boolean existsById(UUID tagId) {
        return tagRepository.existsById(tagId);
    }

    public List<TagDTOOutput> getTagsByFilter(TagFilterDTO filter) {
        return tagListMapper.toDTOList(tagRepository.findAll(TagSpecification.filterBy(filter)));
    }

    public List<TaskTagDTO> getTaskTagsByFilter(TaskTagDTO filter) {
        User currentUser = userService.getCurrentUser();

        Specification<TaskTag> spec = TaskTagSpecification.filterBy(filter);

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            spec = spec.and(TaskTagSpecification.accessibleBy(currentUser));
        }

        List<TaskTag> taskTags = taskTagRepository.findAll(spec);
        return taskTagListMapper.toDTOList(taskTags);
    }

    public TagDTOOutput createUpdateTag(TagDTOInput tagDTOInput) {
        User currentUser = userService.getCurrentUser();

        Tag tag;

        if (tagDTOInput.getId() != null) {
            tag = tagRepository.findById(tagDTOInput.getId())
                    .orElseThrow(()-> new NotFoundException("Tag not found with id: " + tagDTOInput.getId()));

            if (!(tagDTOInput.getName() == null || tagDTOInput.getName().isBlank())) {
                tag.setName(tagDTOInput.getName());
            }

            if (currentUser.getRole() != UserRole.ROLE_ADMIN && !tag.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Not allowed to edit this entity");
            }

            if (!(tagDTOInput.getName() == null || tagDTOInput.getName().isBlank())) {
                tag.setName(tagDTOInput.getName());
            }

            return tagMapper.toDTO(tagRepository.save(tag));
        } else {
            tag = tagMapper.toTag(tagDTOInput);
            return tagMapper.toDTO(tagRepository.save(tag));
        }
    }

    public TaskTagDTO attachTag (UUID taskId, UUID tagId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new NotFoundException("Task not found with id: " + taskId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(()-> new NotFoundException("Tag not found with id: " + tagId));

        if (taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)) {
            throw new IllegalStateException("Tag " + tagId + " is already attached to task " + taskId);
        }

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {

            boolean isTaskAuthor =
                    task.getCreatedBy() != null &&
                            task.getCreatedBy().getId().equals(currentUser.getId());

            ProjectColumn column = task.getColumn();
            boolean isColumnAuthor =
                    column.getCreatedBy() != null &&
                            column.getCreatedBy().getId().equals(currentUser.getId());

            Project project = column.getProject();
            boolean isProjectAuthor =
                    project.getCreatedBy() != null &&
                            project.getCreatedBy().getId().equals(currentUser.getId());

            if (!isTaskAuthor && !isColumnAuthor && !isProjectAuthor) {
                throw new AccessDeniedException(
                        "You do not have permission to attach a tag to this task"
                );
            }
        }

        if (taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)) {
            throw new IllegalStateException("Tag " + tagId + " is already attached to task " + taskId);
        }

        TaskTag taskTagRelation = new TaskTag();
        taskTagRelation.setTask(task);
        taskTagRelation.setTag(tag);

        return taskTagMapper.toDTO(taskTagRepository.save(taskTagRelation));
    }

    public void deleteTag(UUID tagId) {
        User currentUser = userService.getCurrentUser();

        Tag existingTag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found with id: " + tagId));

        if (currentUser.getRole() != UserRole.ROLE_ADMIN && !existingTag.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not allowed to delete this entity");
        }

        tagRepository.delete(existingTag);
    }

    public void deleteTaskTag(UUID taskTagId) {
        User currentUser = userService.getCurrentUser();

        TaskTag existingTaskTag = taskTagRepository.findById(taskTagId)
                .orElseThrow(() -> new NotFoundException("TaskTag relation not found with id: " + taskTagId));

        Task task = existingTaskTag.getTask();

        if (currentUser.getRole() != UserRole.ROLE_ADMIN && !task.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not allowed to delete this relation");
        }

        taskTagRepository.delete(existingTaskTag);
    }
}
