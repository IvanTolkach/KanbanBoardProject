package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.DocumentService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.ForbiddenException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.*;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document.DocumentListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document.DocumentMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.DocumentRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserTaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.DocumentSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private DocumentListMapper documentListMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserTaskRepository userTaskRepository;

    @Captor
    private ArgumentCaptor<Specification<Document>> specCaptor;

    private User currentUser;
    private Task task;
    private ProjectColumn column;
    private Project project;

    private final String uploadDirectory = "test-uploads";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDirectory", uploadDirectory);

        currentUser = new User();
        currentUser.setId(UUID.fromString("10000000-0000-0000-0000-000000000001"));
        currentUser.setRole(UserRole.ROLE_CLIENT);

        project = new Project();
        User projectAuthor = new User();
        projectAuthor.setId(UUID.fromString("20000000-0000-0000-0000-000000000002"));
        project.setCreatedBy(projectAuthor);

        column = new ProjectColumn();
        User columnAuthor = new User();
        columnAuthor.setId(UUID.fromString("30000000-0000-0000-0000-000000000003"));
        column.setCreatedBy(columnAuthor);
        column.setProject(project);

        task = new Task();
        task.setId(UUID.fromString("40000000-0000-0000-0000-000000000004"));
        User taskAuthor = new User();
        taskAuthor.setId(UUID.fromString("50000000-0000-0000-0000-000000000005"));
        task.setCreatedBy(taskAuthor);
        task.setColumn(column);

        lenient().when(userService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void existsById_returnsTrue() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(true);
        assertTrue(documentService.existsById(id));
    }

    @Test
    void existsById_returnsFalse() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(false);
        assertFalse(documentService.existsById(id));
    }

    @Test
    void getDocumentsByFilter_notAuthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);
        assertThrows(UnauthorizedException.class,
                () -> documentService.getDocumentsByFilter(new DocumentFilterDTO()));
    }

    @Test
    void getDocumentsByFilter_admin_seesAll_withoutAccessibleByFilter() {
        currentUser.setRole(UserRole.ROLE_ADMIN);
        DocumentFilterDTO filter = new DocumentFilterDTO();
        List<Document> docs = List.of(new Document());
        List<DocumentDTOOutput> dtoList = List.of(new DocumentDTOOutput());

        when(documentRepository.findAll(any(Specification.class))).thenReturn(docs);
        when(documentListMapper.toDTOList(docs)).thenReturn(dtoList);

        List<DocumentDTOOutput> result = documentService.getDocumentsByFilter(filter);

        assertEquals(dtoList, result);

        verify(documentRepository).findAll(specCaptor.capture());
        String specString = specCaptor.getValue().toString();
        assertTrue(specString.contains("filterBy") || specString.contains(DocumentSpecification.class.getName()));
        assertFalse(specString.contains("accessibleBy"));
    }

    @Test
    void getDocumentsByFilter_nonAdmin_appliesAccessibleByFilter() {
        currentUser.setRole(UserRole.ROLE_CLIENT);
        DocumentFilterDTO filter = new DocumentFilterDTO();
        List<Document> docs = List.of(new Document());
        List<DocumentDTOOutput> dtoList = List.of(new DocumentDTOOutput());

        try (MockedStatic<DocumentSpecification> mockedSpec = mockStatic(DocumentSpecification.class)) {
            Specification<Document> filterSpec = mock(Specification.class);
            Specification<Document> accessSpec = mock(Specification.class);

            when(DocumentSpecification.filterBy(filter)).thenReturn(filterSpec);
            when(DocumentSpecification.accessibleBy(currentUser)).thenReturn(accessSpec);

            when(filterSpec.and(accessSpec)).thenReturn(filterSpec);

            when(documentRepository.findAll(any(Specification.class))).thenReturn(docs);
            when(documentListMapper.toDTOList(docs)).thenReturn(dtoList);

            documentService.getDocumentsByFilter(filter);

            mockedSpec.verify(() -> DocumentSpecification.accessibleBy(currentUser));
            verify(documentRepository).findAll(filterSpec.and(accessSpec));
        }
    }

    @Test
    void downloadDocument_notAuthenticated_throwsUnauthorized() {
        when(userService.getCurrentUser()).thenReturn(null);
        assertThrows(UnauthorizedException.class,
                () -> documentService.downloadDocument(UUID.randomUUID()));
    }

    @Test
    void downloadDocument_documentNotFound_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> documentService.downloadDocument(id));
    }

    @Test
    void downloadDocument_fileNotExists_throwsNotFound() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(tempDir.resolve("nonexistent.txt").toString());

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        assertThrows(NotFoundException.class,
                () -> documentService.downloadDocument(id));
    }

    @Test
    void downloadDocument_admin_canDownload() throws IOException {
        currentUser.setRole(UserRole.ROLE_ADMIN);
        UUID docId = UUID.randomUUID();
        Path file = Files.createTempFile(tempDir, "doc", ".txt");
        Files.writeString(file, "content");

        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        Resource resource = documentService.downloadDocument(docId);
        assertTrue(resource.exists());
        assertEquals(7, resource.contentLength());
    }

    @Test
    void downloadDocument_userIsTaskAuthor_canDownload() throws IOException {
        currentUser.setId(task.getCreatedBy().getId());
        currentUser.setRole(UserRole.ROLE_CLIENT);

        Path file = Files.createTempFile(tempDir, "allowed", ".txt");
        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> documentService.downloadDocument(UUID.randomUUID()));
    }

    @Test
    void downloadDocument_userIsTaskParticipant_canDownload() throws IOException {
        currentUser.setRole(UserRole.ROLE_CLIENT);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(eq(task.getId()), eq(currentUser.getId()), eq(true)))
                .thenReturn(true);

        Path file = Files.createTempFile(tempDir, "part", ".txt");
        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> documentService.downloadDocument(UUID.randomUUID()));
    }

    @Test
    void downloadDocument_userIsColumnAuthor_canDownload() throws IOException {
        currentUser.setId(column.getCreatedBy().getId());
        currentUser.setRole(UserRole.ROLE_CLIENT);

        Path file = Files.createTempFile(tempDir, "col", ".txt");
        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> documentService.downloadDocument(UUID.randomUUID()));
    }

    @Test
    void downloadDocument_userIsProjectAuthor_canDownload() throws IOException {
        currentUser.setId(project.getCreatedBy().getId());
        currentUser.setRole(UserRole.ROLE_CLIENT);

        Path file = Files.createTempFile(tempDir, "proj", ".txt");
        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> documentService.downloadDocument(UUID.randomUUID()));
    }

    @Test
    void downloadDocument_userNoRights_throwsUnauthorized() throws IOException {
        currentUser.setRole(UserRole.ROLE_CLIENT);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(any(), any(), eq(true))).thenReturn(false);

        Path file = Files.createTempFile(tempDir, "noaccess", ".txt");
        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        assertThrows(ForbiddenException.class,
                () -> documentService.downloadDocument(UUID.randomUUID()));
    }

    @Test
    void uploadCreate_notAuthenticated_throws() {
        when(userService.getCurrentUser()).thenReturn(null);
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        assertThrows(UnauthorizedException.class,
                () -> documentService.uploadUpdateDocument(UUID.randomUUID(), null, file));
    }

    @Test
    void uploadCreate_taskNotFound_throws() {
        when(taskRepository.findById(any())).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        assertThrows(NotFoundException.class,
                () -> documentService.uploadUpdateDocument(UUID.randomUUID(), null, file));
    }

    @Test
    void uploadCreate_emptyFile_throws() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID taskId = task.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> documentService.uploadUpdateDocument(taskId, null, file));
    }

    @Test
    void uploadCreate_admin_success() {
        currentUser.setRole(UserRole.ROLE_ADMIN);
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));
        when(documentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(documentMapper.toDTO(any())).thenReturn(new DocumentDTOOutput());

        MockMultipartFile file = new MockMultipartFile("file", "admin.txt", "text/plain", "data".getBytes());

        assertDoesNotThrow(() -> documentService.uploadUpdateDocument(task.getId(), null, file));
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void uploadCreate_userHasRights_success() {
        currentUser.setId(task.getCreatedBy().getId());
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));
        when(documentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(documentMapper.toDTO(any())).thenReturn(new DocumentDTOOutput());

        MockMultipartFile file = new MockMultipartFile("file", "ok.txt", "text/plain", "data".getBytes());

        assertDoesNotThrow(() -> documentService.uploadUpdateDocument(task.getId(), null, file));
    }

    @Test
    void uploadCreate_userNoRights_throws() {
        currentUser.setRole(UserRole.ROLE_CLIENT);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(any(), any(), eq(true))).thenReturn(false);
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));

        MockMultipartFile file = new MockMultipartFile("file", "no.txt", "text/plain", "data".getBytes());

        assertThrows(ForbiddenException.class,
                () -> documentService.uploadUpdateDocument(task.getId(), null, file));
    }

    @Test
    void uploadCreate_directoryDoesNotExist_createsDirectory() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID taskId = task.getId();

        Path nonExistentDir = tempDir.resolve("non-existent-subdir");
        ReflectionTestUtils.setField(documentService, "uploadDirectory", nonExistentDir.toString());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(documentRepository.save(any())).thenReturn(new Document());
        when(documentMapper.toDTO(any())).thenReturn(new DocumentDTOOutput());

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        documentService.uploadUpdateDocument(taskId, null, file);

        assertTrue(Files.exists(nonExistentDir));
    }

    @Test
    void uploadCreate_ioExceptionOnWrite_throwsRuntimeException() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID taskId = task.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class)))
                    .thenThrow(new IOException("Upload failure simulation"));

            assertThrows(RuntimeException.class,
                    () -> documentService.uploadUpdateDocument(taskId, null, file));
        }
    }

    @Test
    void uploadUpdate_documentNotFound_throws() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID taskId = task.getId();
        UUID docId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "data".getBytes());

        assertThrows(NotFoundException.class,
                () -> documentService.uploadUpdateDocument(taskId, docId, file));
    }

    @Test
    void uploadUpdate_changeTask_successIfHasRights() throws IOException {
        currentUser.setRole(UserRole.ROLE_CLIENT);
        currentUser.setId(task.getCreatedBy().getId());

        Task newTask = new Task();
        newTask.setId(UUID.randomUUID());
        newTask.setCreatedBy(currentUser);
        ProjectColumn newColumn = new ProjectColumn();
        newColumn.setCreatedBy(new User());
        newColumn.getCreatedBy().setId(UUID.randomUUID());
        Project newProject = new Project();
        newProject.setCreatedBy(new User());
        newProject.getCreatedBy().setId(UUID.randomUUID());
        newColumn.setProject(newProject);
        newTask.setColumn(newColumn);

        Document existing = new Document();
        existing.setTask(task);
        Path oldFile = Files.createTempFile(tempDir, "old", ".txt");
        Files.writeString(oldFile, "old content");
        existing.setFilePath(oldFile.toString());

        UUID docId = UUID.randomUUID();

        when(taskRepository.findById(newTask.getId())).thenReturn(Optional.of(newTask));
        when(documentRepository.findById(docId)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any())).thenReturn(existing);
        when(documentMapper.toDTO(any())).thenReturn(new DocumentDTOOutput());

        MockMultipartFile file = new MockMultipartFile("file", "new.txt", "text/plain", "new content".getBytes());

        DocumentDTOOutput result = documentService.uploadUpdateDocument(newTask.getId(), docId, file);

        assertNotNull(result);
        assertEquals(newTask, existing.getTask());
        assertFalse(Files.exists(oldFile));
    }

    @Test
    void uploadUpdate_ioExceptionOnDeleteOrWrite_throwsRuntimeException() throws IOException {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID taskId = task.getId();
        UUID docId = UUID.randomUUID();

        Document existing = new Document();
        existing.setTask(task);
        Path oldFile = Files.createTempFile(tempDir, "old", ".txt");
        existing.setFilePath(oldFile.toString());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(documentRepository.findById(docId)).thenReturn(Optional.of(existing));

        MockMultipartFile file = new MockMultipartFile("file", "new.txt", "text/plain", "new data".getBytes());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("Delete failure simulation"));

            assertThrows(RuntimeException.class,
                    () -> documentService.uploadUpdateDocument(taskId, docId, file));
        }
    }

    @Test
    void uploadUpdate_moveToDifferentTask_withoutRights_throws() throws IOException {
        currentUser.setRole(UserRole.ROLE_CLIENT);

        Task newTask = new Task();
        newTask.setId(UUID.randomUUID());

        User newTaskAuthor = new User();
        newTaskAuthor.setId(UUID.randomUUID());
        newTask.setCreatedBy(newTaskAuthor);

        ProjectColumn newColumn = new ProjectColumn();
        User newColumnAuthor = new User();
        newColumnAuthor.setId(UUID.randomUUID());
        newColumn.setCreatedBy(newColumnAuthor);

        Project newProject = new Project();
        User newProjectAuthor = new User();
        newProjectAuthor.setId(UUID.randomUUID());
        newProject.setCreatedBy(newProjectAuthor);

        newColumn.setProject(newProject);
        newTask.setColumn(newColumn);

        Document existing = new Document();
        existing.setTask(task);
        Path oldFile = Files.createTempFile(tempDir, "old", ".txt");
        existing.setFilePath(oldFile.toString());

        UUID existingDocId = UUID.randomUUID();

        when(taskRepository.findById(newTask.getId())).thenReturn(Optional.of(newTask));
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(any(), any(), eq(true)))
                .thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "move.txt", "text/plain", "data".getBytes());

        assertThrows(ForbiddenException.class,
                () -> documentService.uploadUpdateDocument(newTask.getId(), existingDocId, file));
    }

    @Test
    void uploadUpdate_success_replacesFile() throws IOException {
        currentUser.setId(task.getCreatedBy().getId());

        Document existing = new Document();
        Path oldFile = Files.createTempFile(tempDir, "old", ".txt");
        Files.writeString(oldFile, "old content");
        existing.setFilePath(oldFile.toString());
        existing.setTask(task);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(documentRepository.findById(any())).thenReturn(Optional.of(existing));
        when(documentRepository.save(any())).thenReturn(existing);
        when(documentMapper.toDTO(any())).thenReturn(new DocumentDTOOutput());

        MockMultipartFile newFile = new MockMultipartFile("file", "new.txt", "text/plain", "new data".getBytes());

        DocumentDTOOutput result = documentService.uploadUpdateDocument(task.getId(), UUID.randomUUID(), newFile);

        assertNotNull(result);
        assertFalse(Files.exists(oldFile));
        verify(documentRepository).save(existing);
    }

    @Test
    void uploadUpdate_toDifferentTask_withoutRightsOnNew_throwsSpecific() throws IOException {
        currentUser.setRole(UserRole.ROLE_CLIENT);

        UUID oldTaskId = UUID.randomUUID();
        Task oldTask = new Task();
        oldTask.setId(oldTaskId);
        oldTask.setCreatedBy(currentUser);
        ProjectColumn oldColumn = new ProjectColumn();
        oldColumn.setCreatedBy(currentUser);
        Project oldProject = new Project();
        oldProject.setCreatedBy(currentUser);
        oldColumn.setProject(oldProject);
        oldTask.setColumn(oldColumn);

        Document existing = new Document();
        existing.setTask(oldTask);
        Path oldFile = Files.createTempFile(tempDir, "old", ".txt");
        existing.setFilePath(oldFile.toString());

        UUID docId = UUID.randomUUID();
        UUID newTaskId = UUID.randomUUID();

        Task newTaskMock = Mockito.mock(Task.class);
        when(newTaskMock.getId()).thenReturn(newTaskId);

        User newTaskAuthorLater = new User();
        newTaskAuthorLater.setId(UUID.randomUUID());
        when(newTaskMock.getCreatedBy()).thenReturn(currentUser, newTaskAuthorLater);

        ProjectColumn initialColumn = Mockito.mock(ProjectColumn.class);
        when(initialColumn.getCreatedBy()).thenReturn(currentUser);
        Project initialProject = Mockito.mock(Project.class);
        when(initialProject.getCreatedBy()).thenReturn(currentUser);
        when(initialColumn.getProject()).thenReturn(initialProject);

        ProjectColumn newColumn = Mockito.mock(ProjectColumn.class);
        User newColumnAuthor = new User();
        newColumnAuthor.setId(UUID.randomUUID());
        when(newColumn.getCreatedBy()).thenReturn(newColumnAuthor);
        Project newProject = Mockito.mock(Project.class);
        User newProjectAuthor = new User();
        newProjectAuthor.setId(UUID.randomUUID());

        when(newProject.getCreatedBy()).thenReturn(newProjectAuthor);
        when(newColumn.getProject()).thenReturn(newProject);
        when(newTaskMock.getColumn()).thenReturn(initialColumn, newColumn);
        when(taskRepository.findById(newTaskId)).thenReturn(Optional.of(newTaskMock));
        when(documentRepository.findById(docId)).thenReturn(Optional.of(existing));
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(any(), any(), eq(true))).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "move.txt", "text/plain", "data".getBytes());

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> documentService.uploadUpdateDocument(newTaskId, docId, file));

        assertEquals("Not allowed to edit this entity to new task", ex.getMessage());
    }

    @Test
    void deleteDocument_notAuthenticated_throws() {
        when(userService.getCurrentUser()).thenReturn(null);
        assertThrows(UnauthorizedException.class,
                () -> documentService.deleteDocument(UUID.randomUUID()));
    }

    @Test
    void deleteDocument_notFound_throws() {
        when(documentRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> documentService.deleteDocument(UUID.randomUUID()));
    }

    @Test
    void deleteDocument_noRights_throws() throws IOException {
        currentUser.setRole(UserRole.ROLE_CLIENT);
        when(userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(any(), any(), eq(true))).thenReturn(false);

        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(Files.createTempFile(tempDir, "todel", ".txt").toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        assertThrows(ForbiddenException.class,
                () -> documentService.deleteDocument(UUID.randomUUID()));
    }

    @Test
    void deleteDocument_success_deletesFileAndEntity() throws IOException {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        Path file = Files.createTempFile(tempDir, "todelete", ".txt");
        Files.writeString(file, "delete me");

        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        when(documentRepository.findById(any())).thenReturn(Optional.of(doc));

        documentService.deleteDocument(UUID.randomUUID());

        verify(documentRepository).deleteById(any());
        assertFalse(Files.exists(file));
    }

    @Test
    void deleteDocument_fileDeleteFails_stillDeletesFromDb() throws IOException {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        Path file = Files.createTempFile(tempDir, "todelete", ".txt");
        Files.writeString(file, "content");

        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath(file.toString());

        UUID docId = UUID.randomUUID();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {
            mockedFiles.when(() -> Files.delete(any(Path.class)))
                    .thenThrow(new IOException("Disk is on fire!"));

            assertDoesNotThrow(() -> documentService.deleteDocument(docId));

            verify(documentRepository).deleteById(docId);
        }
    }

    @Test
    void deleteDocument_ioExceptionOnFileDelete_throwsRuntime() {
        currentUser.setRole(UserRole.ROLE_ADMIN);

        UUID docId = UUID.randomUUID();

        Document doc = new Document();
        doc.setTask(task);
        doc.setFilePath("dummy/path.txt");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("Delete error simulation"));

            assertThrows(RuntimeException.class, () -> documentService.deleteDocument(docId));

            verify(documentRepository, never()).deleteById(docId);
        }
    }
}