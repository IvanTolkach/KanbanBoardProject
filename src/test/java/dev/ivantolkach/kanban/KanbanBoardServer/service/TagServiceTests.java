package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tag.TagFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.tasktag.TaskTagDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.TagService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.ForbiddenException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

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

    @Mock
    private UserService userService;

    private User currentUser;

    @BeforeEach
    void setup() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setRole(UserRole.ROLE_CLIENT);

        Mockito.lenient().when(userService.getCurrentUser()).thenReturn(currentUser);
    }

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
    void getTaskTagsByFilter_returnsFilteredTaskTags_asAdmin() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

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
    void getTaskTagsByFilter_noMatches_returnsEmptyList_asAdmin() {
        currentUser.setRole(UserRole.ROLE_ADMIN);
        TaskTagDTO filter = new TaskTagDTO();

        when(taskTagRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(taskTagListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<TaskTagDTO> result = tagService.getTaskTagsByFilter(filter);

        assertTrue(result.isEmpty());
        verify(taskTagRepository).findAll(any(Specification.class));
        verify(taskTagListMapper).toDTOList(List.of());
    }

    @Test
    void getTaskTagsByFilter_unauthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);

        TaskTagDTO filter = new TaskTagDTO();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> tagService.getTaskTagsByFilter(filter));

        assertEquals("User is not authenticated", ex.getMessage());

        verifyNoInteractions(taskTagRepository, taskTagListMapper);
    }

    @Test
    void getTaskTagsByFilter_nonAdmin_executesAccessibleByBranch() {
        currentUser.setRole(UserRole.ROLE_CLIENT);

        TaskTagDTO filter = new TaskTagDTO();
        TaskTag tt = new TaskTag();
        tt.setId(UUID.randomUUID());
        List<TaskTag> taskTags = List.of(tt);
        List<TaskTagDTO> expected = List.of(new TaskTagDTO());

        when(taskTagRepository.findAll(any(Specification.class))).thenReturn(taskTags);
        when(taskTagListMapper.toDTOList(taskTags)).thenReturn(expected);

        List<TaskTagDTO> result = tagService.getTaskTagsByFilter(filter);

        assertEquals(expected, result);
        verify(userService).getCurrentUser();
        verify(taskTagRepository).findAll(any(Specification.class));
        verify(taskTagListMapper).toDTOList(taskTags);
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
        tag.setCreatedBy(currentUser);
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
    void createUpdateTag_unauthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);

        TagDTOInput input = new TagDTOInput();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> tagService.createUpdateTag(input));

        assertEquals("User is not authenticated", ex.getMessage());

        verifyNoInteractions(tagRepository, tagMapper, tagListMapper,
                taskTagRepository, taskTagMapper, taskTagListMapper,
                taskRepository);
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
        tag.setCreatedBy(currentUser);
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
        tag.setCreatedBy(currentUser);
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
    void updateTag_notOwner_throwsUnauthorized() {
        UUID tagId = UUID.randomUUID();
        TagDTOInput input = new TagDTOInput();
        input.setId(tagId);
        input.setName("UpdatedTag");
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("OldTag");
        User other = new User();
        other.setId(UUID.randomUUID());
        tag.setCreatedBy(other);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> tagService.createUpdateTag(input));

        assertEquals("Not allowed to edit this entity", ex.getMessage());
        verify(tagRepository).findById(tagId);
        verify(tagRepository, never()).save(any());
    }

    @Test
    void attachTag_success() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setCreatedBy(currentUser);

        ProjectColumn column = new ProjectColumn();
        column.setId(UUID.randomUUID());
        column.setCreatedBy(currentUser);
        column.setProject(project);

        Task task = new Task();
        task.setId(taskId);
        task.setCreatedBy(currentUser);
        task.setColumn(column);

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
        verify(taskTagRepository, times(2)).existsByTaskIdAndTagId(taskId, tagId);

        verify(taskTagRepository).save(any(TaskTag.class));
        verify(taskTagMapper).toDTO(taskTag);
    }

    @Test
    void attachTag_unauthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);

        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> tagService.attachTag(taskId, tagId));

        assertEquals("User is not authenticated", ex.getMessage());

        verifyNoInteractions(taskRepository, tagRepository, taskTagRepository, taskTagMapper);
    }

    @Test
    void attachTag_alreadyAttached_throwsIllegalStateException_specific() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);
        task.setCreatedBy(currentUser);

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

        verify(taskTagRepository, never()).save(any(TaskTag.class));
        verify(taskTagMapper, never()).toDTO(any(TaskTag.class));
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
        task.setCreatedBy(currentUser);

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
        task.setCreatedBy(currentUser);
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
    void attachTag_accessDenied_whenNotAuthor() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);

        User other = new User();
        other.setId(UUID.randomUUID());
        task.setCreatedBy(other);

        ProjectColumn column = new ProjectColumn();
        User colAuthor = new User();
        colAuthor.setId(UUID.randomUUID());
        column.setCreatedBy(colAuthor);
        Project project = new Project();
        User projAuthor = new User();
        projAuthor.setId(UUID.randomUUID());
        project.setCreatedBy(projAuthor);
        column.setProject(project);
        task.setColumn(column);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(new Tag()));
        when(taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> tagService.attachTag(taskId, tagId));

        assertEquals("You do not have permission to attach a tag to this task", ex.getMessage());
        verify(taskRepository).findById(taskId);
        verify(tagRepository).findById(tagId);
    }

    @Test
    void attachTag_alreadyAttached_onSecondExistsCheck_throwsIllegalStateException() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Project project = new Project();
        project.setCreatedBy(currentUser);

        ProjectColumn column = new ProjectColumn();
        column.setProject(project);
        column.setCreatedBy(currentUser);

        Task task = new Task();
        task.setId(taskId);
        task.setCreatedBy(currentUser);
        task.setColumn(column);

        Tag tag = new Tag();
        tag.setId(tagId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        when(taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)).thenReturn(false, true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> tagService.attachTag(taskId, tagId)
        );

        assertEquals("Tag " + tagId + " is already attached to task " + taskId, ex.getMessage());

        verify(taskRepository).findById(taskId);
        verify(tagRepository).findById(tagId);
        verify(taskTagRepository, times(2)).existsByTaskIdAndTagId(taskId, tagId);

        verify(taskTagRepository, never()).save(any());
        verify(taskTagMapper, never()).toDTO(any());
    }

    @Test
    void deleteTag_success() {
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setCreatedBy(currentUser);

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
    void deleteTag_notOwner_throwsUnauthorized() {
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(tagId);
        User other = new User();
        other.setId(UUID.randomUUID());
        tag.setCreatedBy(other);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> tagService.deleteTag(tagId));

        assertEquals("Not allowed to delete this entity", ex.getMessage());
        verify(tagRepository).findById(tagId);
        verify(tagRepository, never()).delete(any(Tag.class));
    }

    @Test
    void deleteTaskTag_success() {
        UUID taskTagId = UUID.randomUUID();
        TaskTag taskTag = new TaskTag();
        taskTag.setId(taskTagId);

        Task task = new Task();
        task.setCreatedBy(currentUser);
        taskTag.setTask(task);

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

    @Test
    void deleteTaskTag_notOwner_throwsUnauthorized() {
        UUID taskTagId = UUID.randomUUID();
        TaskTag taskTag = new TaskTag();
        taskTag.setId(taskTagId);

        Task task = new Task();
        User other = new User();
        other.setId(UUID.randomUUID());
        task.setCreatedBy(other);
        taskTag.setTask(task);

        when(taskTagRepository.findById(taskTagId)).thenReturn(Optional.of(taskTag));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> tagService.deleteTaskTag(taskTagId));

        assertEquals("Not allowed to delete this relation", ex.getMessage());
        verify(taskTagRepository).findById(taskTagId);
        verify(taskTagRepository, never()).delete(any(TaskTag.class));
    }

    @Test
    void deleteTag_unauthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);

        UUID tagId = UUID.randomUUID();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> tagService.deleteTag(tagId));

        assertEquals("User is not authenticated", ex.getMessage());

        verifyNoInteractions(tagRepository, taskTagRepository, tagMapper,
                taskTagMapper, taskTagListMapper, tagListMapper,
                taskRepository);
    }

    @Test
    void deleteTaskTag_unauthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);

        UUID taskTagId = UUID.randomUUID();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> tagService.deleteTaskTag(taskTagId));

        assertEquals("User is not authenticated", ex.getMessage());

        verifyNoInteractions(taskTagRepository, tagRepository, taskRepository,
                taskTagMapper, tagMapper, tagListMapper, taskTagListMapper);
    }

}