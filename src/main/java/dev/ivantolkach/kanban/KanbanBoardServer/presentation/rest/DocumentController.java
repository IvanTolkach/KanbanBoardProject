package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.DocumentService;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.DocumentEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class DocumentController implements DocumentEndpoint {

    @Autowired
    DocumentService documentService;

    @Override
    public List<DocumentDTOOutput> getDocuments(DocumentFilterDTO filter) {
        return documentService.getDocumentsByFilter(filter);
    }

    @Override
    public Resource downloadDocument(UUID documentId) {
        return documentService.downloadDocument(documentId);
    }

    @Override
    public DocumentDTOOutput uploadUpdateDocument(UUID taskId, UUID documentId, MultipartFile file) {
        return documentService.uploadUpdateDocument(taskId, documentId, file);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(UUID documentId) {
        documentService.deleteDocument(documentId);
    }
}
