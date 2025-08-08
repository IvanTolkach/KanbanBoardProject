package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.NotFoundException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Document;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.Task;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document.DocumentListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.document.DocumentMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.DocumentRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.TaskRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.DocumentSpecification;

import org.hibernate.type.descriptor.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    @Value("${upload.path:/uploads}")
    private String uploadDirectory;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentListMapper documentListMapper;

    @Autowired
    private TaskRepository taskRepository;

    public boolean existsById(UUID documentId) {
        return documentRepository.existsById(documentId);
    }

    public List<DocumentDTOOutput> getDocumentsByFilter(DocumentFilterDTO filter) {
        return documentListMapper.toDTOList(documentRepository.findAll(DocumentSpecification.filterBy(filter)));
    }

    public Resource downloadDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found with id: " + documentId));

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new NotFoundException("Resource not found with.");
        }

        return resource;
    }

    public DocumentDTOOutput uploadUpdateDocument(UUID taskId, UUID documentId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        if (documentId == null) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            File directory = new File(uploadDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDirectory, fileName);

            try {
                Files.write(filePath, file.getBytes());

                Document document = new Document();
                document.setTask(task);
                document.setFileName(fileName);
                document.setFileType(file.getContentType());
                document.setFileSize(file.getSize());
                document.setFilePath(filePath.toString());

                return documentMapper.toDTO(documentRepository.save(document));

            } catch (IOException e) {
                throw new RuntimeException("Error while uploading file", e);
            }
        }
        else {
            Document existingDocument = documentRepository.findById(documentId)
                    .orElseThrow(() -> new NotFoundException("Document not found with id: " + documentId));

            try {
                Files.deleteIfExists(Paths.get(existingDocument.getFilePath()));

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDirectory, fileName);

                Files.write(filePath, file.getBytes());

                existingDocument.setTask(task);
                existingDocument.setFileName(fileName);
                existingDocument.setFileType(file.getContentType());
                existingDocument.setFileSize(file.getSize());
                existingDocument.setFilePath(filePath.toString());

                return documentMapper.toDTO(documentRepository.save(existingDocument));
            } catch (IOException e) {
                throw new RuntimeException("Error while deleting or uploading file", e); }
        }
    }

    public void deleteDocument(UUID documentId) {
        Document existingDocument = documentRepository.findById(documentId)
                .orElseThrow(()->new NotFoundException("Document not found with id: " + documentId));

        try {
            Files.deleteIfExists(Paths.get(existingDocument.getFilePath()));
            documentRepository.deleteById(documentId);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error while deleting file", e);
        }
    }
}
