package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.document.DocumentFilterDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentEndpoint {
    @GetMapping(ApiEndpoints.Document.BASE)
    List<DocumentDTOOutput> getDocuments(
            @RequestBody DocumentFilterDTO filter
    );

    @GetMapping(ApiEndpoints.Document.BY_ID)
    ResponseEntity<Resource> downloadDocument(
            @PathVariable UUID documentId
    );

    @PutMapping(ApiEndpoints.Document.UPLOAD_UPDATE)
    ResponseEntity<DocumentDTOOutput> uploadUpdateDocument(
            @PathVariable UUID taskId,
            @RequestParam(name = "documentId", required = false) UUID documentId,
            @RequestParam("file")MultipartFile file
    );

    @DeleteMapping(ApiEndpoints.Document.BY_ID)
    ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId
    );
}
