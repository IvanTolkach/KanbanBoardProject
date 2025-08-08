package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Project;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Tag;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.TaskTag;
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

    public boolean existsById(UUID tagId) {
        return tagRepository.existsById(tagId);
    }

    public List<TagDTOOutput> getTagsByFilter(TagFilterDTO filter) {
        return tagListMapper.toDTOList(tagRepository.findAll(TagSpecification.filterBy(filter)));
    }

    public List<TaskTagDTO> getTaskTagsByFilter(TaskTagDTO filter) {
        return taskTagListMapper.toDTOList(taskTagRepository.findAll(TaskTagSpecification.filterBy(filter)));
    }

    public TagDTOOutput createUpdateTag(TagDTOInput tagDTOInput) {

        Tag tag;

        if (tagDTOInput.getId() != null) {
            tag = tagRepository.findById(tagDTOInput.getId())
                    .orElseThrow(()-> new NotFoundException("Tag not found with id: " + tagDTOInput.getId()));

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
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new NotFoundException("Task not found with id: " + taskId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(()-> new NotFoundException("Tag not found with id: " + tagId));

        if (taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)) {
            throw new IllegalStateException("Tag " + tagId + " is already attached to task " + taskId);
        }

        TaskTag taskTagRelation = new TaskTag();
        taskTagRelation.setTask(task);
        taskTagRelation.setTag(tag);

        return taskTagMapper.toDTO(taskTagRepository.save(taskTagRelation));
    }

    public void deleteTag(UUID tagId) {
        Tag existingTag = tagRepository.findById(tagId)
                .orElseThrow(()-> new NotFoundException("Tag not found with id: " + tagId));

        tagRepository.delete(existingTag);
    }

    public void deleteTaskTag(UUID taskTagId) {
        TaskTag existingTaskTag = taskTagRepository.findById(taskTagId)
                .orElseThrow(()->new NotFoundException("TaskTag relation not found with id: " + taskTagId));

        taskTagRepository.delete(existingTaskTag);
    }
}
