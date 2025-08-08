package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserFilterDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface UserEndpoint {
    @GetMapping(ApiEndpoints.User.BASE)
    List<UserDTOOutput> getUsers(
            @RequestBody UserFilterDTO filter
    );

    @PutMapping(ApiEndpoints.User.BASE)
    ResponseEntity<UserDTOOutput> createUpdateUser(
            @Valid @RequestBody UserDTOInput user
    );

    @PutMapping(ApiEndpoints.User.CHANGE_PASSWORD)
    ResponseEntity<UserDTOOutput> changeUserPassword(
            @PathVariable UUID userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    );

    @DeleteMapping(ApiEndpoints.User.BY_ID)
    ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId
    );
}
