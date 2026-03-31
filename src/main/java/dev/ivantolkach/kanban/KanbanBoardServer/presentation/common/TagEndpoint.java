package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface TagEndpoint {
    @GetMapping(ApiEndpoints.Tag.BASE)
    List<TagDTOOutput> getTags(
            @RequestBody TagFilterDTO filter
    );

    @PutMapping(ApiEndpoints.Tag.BASE)
    TagDTOOutput createUpdateTag(
            @Valid @RequestBody TagDTOInput tag
    );

    @DeleteMapping(ApiEndpoints.Tag.BY_ID)
    void deleteTag (
            @PathVariable UUID tagId
    );

    @GetMapping(ApiEndpoints.Tag.TASKS_TAGS)
    List<TaskTagDTO> getTasksTags(
            @RequestBody TaskTagDTO filter
    );

    @PutMapping(ApiEndpoints.Tag.ATTACH_TAG)
    TaskTagDTO attachTag(
            @PathVariable UUID taskId,
            @PathVariable UUID tagId
    );

    @DeleteMapping(ApiEndpoints.Tag.TASKS_TAGS_BY_ID)
    void deleteTaskTag (
            @PathVariable UUID taskTagId
    );
}
