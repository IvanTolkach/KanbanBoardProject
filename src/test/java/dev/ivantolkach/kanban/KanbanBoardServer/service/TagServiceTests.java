package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.TagService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTests {

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TaskTagRepository taskTagRepository;

    @Mock
    private TaskTagMapper taskTagMapper;

    @Mock
    private TaskTagListMapper taskTagListMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TagListMapper tagListMapper;

    @Mock
    private TaskRepository taskRepository;

    @Test
    void getTagsByFilter_returnsFilteredTags() {
        TagFilterDTO filter = new TagFilterDTO();
        filter.setName("TestTag");
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName("TestTag");
        List<Tag> tags = List.of(tag);
        List<TagDTOOutput> expectedDTOs = List.of(new TagDTOOutput());

        when(tagRepository.findAll(any(Specification.class))).thenReturn(tags);
        when(tagListMapper.toDTOList(tags)).thenReturn(expectedDTOs);

        List<TagDTOOutput> result = tagService.getTagsByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(tagRepository).findAll(any(Specification.class));
        verify(tagListMapper).toDTOList(tags);
    }

    @Test
    void getTagsByFilter_noMatches_returnsEmptyList() {
        TagFilterDTO filter = new TagFilterDTO();

        when(tagRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(tagListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<TagDTOOutput> result = tagService.getTagsByFilter(filter);

        assertTrue(result.isEmpty());
        verify(tagRepository).findAll(any(Specification.class));
        verify(tagListMapper).toDTOList(List.of());
    }

    @Test
    void getTaskTagsByFilter_returnsFilteredTaskTags() {
        TaskTagDTO filter = new TaskTagDTO();
        TaskTag taskTag = new TaskTag();
        taskTag.setId(UUID.randomUUID());
        List<TaskTag> taskTags = List.of(taskTag);
        List<TaskTagDTO> expectedDTOs = List.of(new TaskTagDTO());

        when(taskTagRepository.findAll(any(Specification.class))).thenReturn(taskTags);
        when(taskTagListMapper.toDTOList(taskTags)).thenReturn(expectedDTOs);

        List<TaskTagDTO> result = tagService.getTaskTagsByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(taskTagRepository).findAll(any(Specification.class));
        verify(taskTagListMapper).toDTOList(taskTags);
    }

    @Test
    void getTaskTagsByFilter_noMatches_returnsEmptyList() {
        TaskTagDTO filter = new TaskTagDTO();

        when(taskTagRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(taskTagListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<TaskTagDTO> result = tagService.getTaskTagsByFilter(filter);

        assertTrue(result.isEmpty());
        verify(taskTagRepository).findAll(any(Specification.class));
        verify(taskTagListMapper).toDTOList(List.of());
    }

    @Test
    void createNewTag_success() {
        TagDTOInput input = new TagDTOInput();
        input.setId(null);
        input.setName("NewTag");
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName("NewTag");
        TagDTOOutput expectedDTO = new TagDTOOutput();

        when(tagMapper.toTag(input)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(expectedDTO);

        TagDTOOutput result = tagService.createUpdateTag(input);

        assertEquals(expectedDTO, result);
        verify(tagMapper).toTag(input);
        verify(tagRepository).save(tag);
        verify(tagMapper).toDTO(tag);
    }

    @Test
    void updateTag_success() {
        UUID tagId = UUID.randomUUID();
        TagDTOInput input = new TagDTOInput();
        input.setId(tagId);
        input.setName("UpdatedTag");
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("OldTag");
        TagDTOOutput expectedDTO = new TagDTOOutput();

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(expectedDTO);

        TagDTOOutput result = tagService.createUpdateTag(input);

        assertEquals(expectedDTO, result);
        assertEquals("UpdatedTag", tag.getName());
        verify(tagRepository).findById(tagId);
        verify(tagRepository).save(tag);
        verify(tagMapper).toDTO(tag);
    }

    @Test
    void updateTag_tagNotFound_throwsNotFoundException() {
        UUID tagId = UUID.randomUUID();
        TagDTOInput input = new TagDTOInput();
        input.setId(tagId);
        input.setName("UpdatedTag");

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> tagService.createUpdateTag(input));

        verify(tagRepository).findById(tagId);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_nullName_doesNotChangeName() {
        UUID tagId = UUID.randomUUID();
        TagDTOInput input = new TagDTOInput();
        input.setId(tagId);
        input.setName(null);
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("OldTag");
        TagDTOOutput expectedDTO = new TagDTOOutput();

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(expectedDTO);

        TagDTOOutput result = tagService.createUpdateTag(input);

        assertEquals(expectedDTO, result);
        assertEquals("OldTag", tag.getName());
        verify(tagRepository).findById(tagId);
        verify(tagRepository).save(tag);
        verify(tagMapper).toDTO(tag);
    }

    @Test
    void updateTag_blankName_doesNotChangeName() {
        UUID tagId = UUID.randomUUID();
        TagDTOInput input = new TagDTOInput();
        input.setId(tagId);
        input.setName("");
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("OldTag");
        TagDTOOutput expectedDTO = new TagDTOOutput();

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(expectedDTO);

        TagDTOOutput result = tagService.createUpdateTag(input);

        assertEquals(expectedDTO, result);
        assertEquals("OldTag", tag.getName());
        verify(tagRepository).findById(tagId);
        verify(tagRepository).save(tag);
        verify(tagMapper).toDTO(tag);
    }

    @Test
    void attachTag_success() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        Tag tag = new Tag();
        tag.setId(tagId);
        TaskTag taskTag = new TaskTag();
        taskTag.setId(UUID.randomUUID());
        taskTag.setTask(task);
        taskTag.setTag(tag);
        TaskTagDTO expectedDTO = new TaskTagDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)).thenReturn(false);
        when(taskTagRepository.save(any(TaskTag.class))).thenReturn(taskTag);
        when(taskTagMapper.toDTO(taskTag)).thenReturn(expectedDTO);

        TaskTagDTO result = tagService.attachTag(taskId, tagId);

        assertEquals(expectedDTO, result);
        verify(taskRepository).findById(taskId);
        verify(tagRepository).findById(tagId);
        verify(taskTagRepository).existsByTaskIdAndTagId(taskId, tagId);
        verify(taskTagRepository).save(any(TaskTag.class));
        verify(taskTagMapper).toDTO(taskTag);
    }

    @Test
    void attachTag_taskNotFound_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> tagService.attachTag(taskId, tagId));

        assertEquals("Task not found with id: " + taskId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(tagRepository, taskTagRepository, taskTagMapper, tagMapper);
    }

    @Test
    void attachTag_tagNotFound_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> tagService.attachTag(taskId, tagId));

        assertEquals("Tag not found with id: " + tagId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(tagRepository).findById(tagId);
        verifyNoInteractions(taskTagRepository, taskTagMapper, tagMapper);
    }

    @Test
    void attachTag_alreadyAttached_throwsIllegalStateException() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        Tag tag = new Tag();
        tag.setId(tagId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> tagService.attachTag(taskId, tagId));

        assertEquals("Tag " + tagId + " is already attached to task " + taskId, ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(tagRepository).findById(tagId);
        verify(taskTagRepository).existsByTaskIdAndTagId(taskId, tagId);
        verifyNoInteractions(taskTagMapper, tagMapper);
    }

    @Test
    void deleteTag_success() {
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(tagId);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        tagService.deleteTag(tagId);

        verify(tagRepository).findById(tagId);
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteTag_tagNotFound_throwsNotFoundException() {
        UUID tagId = UUID.randomUUID();

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> tagService.deleteTag(tagId));

        assertEquals("Tag not found with id: " + tagId, ex.getMessage());
        verify(tagRepository).findById(tagId);
        verifyNoInteractions(taskTagRepository, taskRepository, tagMapper, taskTagMapper);
    }

    @Test
    void deleteTaskTag_success() {
        UUID taskTagId = UUID.randomUUID();
        TaskTag taskTag = new TaskTag();
        taskTag.setId(taskTagId);

        when(taskTagRepository.findById(taskTagId)).thenReturn(Optional.of(taskTag));

        tagService.deleteTaskTag(taskTagId);

        verify(taskTagRepository).findById(taskTagId);
        verify(taskTagRepository).delete(taskTag);
    }

    @Test
    void deleteTaskTag_taskTagNotFound_throwsNotFoundException() {
        UUID taskTagId = UUID.randomUUID();

        when(taskTagRepository.findById(taskTagId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> tagService.deleteTaskTag(taskTagId));

        assertEquals("TaskTag relation not found with id: " + taskTagId, ex.getMessage());
        verify(taskTagRepository).findById(taskTagId);
        verifyNoInteractions(tagRepository, taskRepository, tagMapper, taskTagMapper);
    }
}