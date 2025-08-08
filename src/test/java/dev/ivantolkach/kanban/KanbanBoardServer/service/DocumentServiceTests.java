package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.DocumentService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Document;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document.DocumentListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document.DocumentMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.DocumentRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setup() {
        documentService = new DocumentService();
        ReflectionTestUtils.setField(documentService, "uploadDirectory", "test-uploads");
        ReflectionTestUtils.setField(documentService, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(documentService, "documentMapper", documentMapper);
        ReflectionTestUtils.setField(documentService, "documentListMapper", documentListMapper);
        ReflectionTestUtils.setField(documentService, "taskRepository", taskRepository);
    }

    @Test
    void existsById_returnsTrue() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(true);
        assertTrue(documentService.existsById(id));
    }

    @Test
    void getDocumentsByFilter_returnsDTOList() {
        DocumentFilterDTO filter = new DocumentFilterDTO();
        List<Document> docs = List.of(new Document());
        List<DocumentDTOOutput> expected = List.of(new DocumentDTOOutput());

        when(documentRepository.findAll(any(Specification.class))).thenReturn(docs);
        when(documentListMapper.toDTOList(docs)).thenReturn(expected);

        List<DocumentDTOOutput> result = documentService.getDocumentsByFilter(filter);

        assertEquals(expected, result);
    }

    @Test
    void downloadDocument_success() throws IOException {
        UUID id = UUID.randomUUID();
        Path path = Files.createTempFile("test", ".txt");
        Files.writeString(path, "sample");

        Document doc = new Document();
        doc.setFilePath(path.toString());

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        Resource res = documentService.downloadDocument(id);
        assertTrue(res.exists());

        Files.deleteIfExists(path);
    }

    @Test
    void downloadDocument_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> documentService.downloadDocument(id));
    }

    @Test
    void downloadDocument_missingFile_throwsException() {
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setFilePath("non-existent-file.txt");

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));
        assertThrows(NotFoundException.class, () -> documentService.downloadDocument(id));
    }

    @Test
    void uploadDocument_successfully() throws IOException {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Document saved = new Document();
        when(documentRepository.save(any())).thenReturn(saved);
        when(documentMapper.toDTO(saved)).thenReturn(new DocumentDTOOutput());

        DocumentDTOOutput result = documentService.uploadUpdateDocument(taskId, null, file);
        assertNotNull(result);
    }

    @Test
    void uploadDocument_withEmptyFile_throwsException() {
        UUID taskId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(new Task()));

        assertThrows(IllegalArgumentException.class, () -> documentService.uploadUpdateDocument(taskId, null, file));
    }

    @Test
    void updateDocument_existing_successfully() throws IOException {
        UUID taskId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();

        Task task = new Task();
        Document doc = new Document();
        Path oldPath = Files.createTempFile("old", ".txt");
        doc.setFilePath(oldPath.toString());

        MockMultipartFile file = new MockMultipartFile("file", "new.txt", "text/plain", "data".getBytes());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(doc)).thenReturn(doc);
        when(documentMapper.toDTO(doc)).thenReturn(new DocumentDTOOutput());

        DocumentDTOOutput result = documentService.uploadUpdateDocument(taskId, docId, file);
        assertNotNull(result);

        Files.deleteIfExists(oldPath);
    }

    @Test
    void updateDocument_notFound_throwsException() {
        UUID taskId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "data".getBytes());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(new Task()));
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> documentService.uploadUpdateDocument(taskId, docId, file));
    }

    @Test
    void deleteDocument_success() throws IOException {
        UUID docId = UUID.randomUUID();
        Document doc = new Document();
        Path filePath = Files.createTempFile("todelete", ".txt");
        Files.writeString(filePath, "to be deleted");
        doc.setFilePath(filePath.toString());

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> documentService.deleteDocument(docId));
        verify(documentRepository).deleteById(docId);

        Files.deleteIfExists(filePath);
    }

    @Test
    void deleteDocument_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> documentService.deleteDocument(id));
    }
}
