package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.TagService;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.TagEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TagController implements TagEndpoint {

    @Autowired
    private TagService tagService;

    @Override
    public List<TagDTOOutput> getTags(TagFilterDTO filter) {
        return tagService.getTagsByFilter(filter);
    }

    @Override
    public List<TaskTagDTO> getTasksTags(TaskTagDTO filter) {
        return tagService.getTaskTagsByFilter(filter);
    }

    @Override
    public ResponseEntity<TagDTOOutput> createUpdateTag(TagDTOInput tag) {
        return ResponseEntity.ok(tagService.createUpdateTag(tag));
    }

    @Override
    public ResponseEntity<TaskTagDTO> attachTag(UUID taskId, UUID tagId) {
        return ResponseEntity.ok(tagService.attachTag(taskId, tagId));
    }

    @Override
    public ResponseEntity<Void> deleteTaskTag(UUID taskTagId) {
        tagService.deleteTaskTag(taskTagId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteTag(UUID tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
