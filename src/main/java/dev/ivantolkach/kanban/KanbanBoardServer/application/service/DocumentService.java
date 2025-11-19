package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Autowired
    private UserService userService;

    @Autowired
    private UserTaskRepository userTaskRepository;

    public boolean existsById(UUID documentId) {
        return documentRepository.existsById(documentId);
    }

    public List<DocumentDTOOutput> getDocumentsByFilter(DocumentFilterDTO filter) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Specification<Document> spec = DocumentSpecification.filterBy(filter);

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            spec = spec.and(DocumentSpecification.accessibleBy(currentUser));
        }

        List<Document> documents = documentRepository.findAll(spec);
        return documentListMapper.toDTOList(documents);
    }

    public Resource downloadDocument(UUID documentId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found with id: " + documentId));

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            Task task = document.getTask();
            ProjectColumn column = task.getColumn();
            Project project = column.getProject();

            boolean isTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
            boolean isTaskParticipant = userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(task.getId(), currentUser.getId(), true);
            boolean isColumnAuthor = column.getCreatedBy().getId().equals(currentUser.getId());
            boolean isProjectAuthor = project.getCreatedBy().getId().equals(currentUser.getId());

            if (! (isTaskAuthor || isTaskParticipant || isColumnAuthor || isProjectAuthor)) {
                throw new ForbiddenException("Not allowed to access this entity");
            }
        }

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new NotFoundException("Resource not found with.");
        }

        return resource;
    }

    public DocumentDTOOutput uploadUpdateDocument(UUID taskId, UUID documentId, MultipartFile file) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            ProjectColumn column = task.getColumn();
            Project project = column.getProject();

            boolean isTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
            boolean isTaskParticipant = userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(task.getId(), currentUser.getId(), true);
            boolean isColumnAuthor = column.getCreatedBy().getId().equals(currentUser.getId());
            boolean isProjectAuthor = project.getCreatedBy().getId().equals(currentUser.getId());

            if (! (isTaskAuthor || isTaskParticipant || isColumnAuthor || isProjectAuthor)) {
                throw new ForbiddenException("Not allowed to create or edit this entity");
            }
        }

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

            if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
                Task existingTask = existingDocument.getTask();
                if (!existingTask.getId().equals(task.getId())) {
                    ProjectColumn newColumn = task.getColumn();
                    Project newProject = newColumn.getProject();

                    boolean isNewTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
                    boolean isNewTaskParticipant = userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(task.getId(), currentUser.getId(), true);
                    boolean isNewColumnAuthor = newColumn.getCreatedBy().getId().equals(currentUser.getId());
                    boolean isNewProjectAuthor = newProject.getCreatedBy().getId().equals(currentUser.getId());

                    if (! (isNewTaskAuthor || isNewTaskParticipant || isNewColumnAuthor || isNewProjectAuthor)) {
                        throw new ForbiddenException("Not allowed to edit this entity to new task");
                    }
                }
            }

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
                throw new RuntimeException("Error while deleting or uploading file", e);
            }
        }
    }

    public void deleteDocument(UUID documentId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Document existingDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found with id: " + documentId));

        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            Task task = existingDocument.getTask();
            ProjectColumn column = task.getColumn();
            Project project = column.getProject();

            boolean isTaskAuthor = task.getCreatedBy().getId().equals(currentUser.getId());
            boolean isTaskParticipant = userTaskRepository.existsByTaskIdAndUserIdAndIsAssigned(task.getId(), currentUser.getId(), true);
            boolean isColumnAuthor = column.getCreatedBy().getId().equals(currentUser.getId());
            boolean isProjectAuthor = project.getCreatedBy().getId().equals(currentUser.getId());

            if (! (isTaskAuthor || isTaskParticipant || isColumnAuthor || isProjectAuthor)) {
                throw new ForbiddenException("Not allowed to delete this entity");
            }
        }

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
