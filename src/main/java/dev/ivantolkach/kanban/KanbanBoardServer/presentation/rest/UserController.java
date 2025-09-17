package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.*;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.UserEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController implements UserEndpoint {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserDTOOutput> getUsers(UserFilterDTO filter) {
        return userService.getUsersByFilter(filter);
    }

    @Override
    public UserDTOOutput getCurrentUser() {
        return userMapper.toDTO(userService.getCurrentUser());
    }

    @Override
    public ResponseEntity<UserDTOOutput> createUpdateUser(UserDTOInput user) {
        return ResponseEntity.ok(userService.createUpdateUser(user));
    }

    @Override
    public ResponseEntity<UserDTOOutput> changeUserPassword(UUID userId, String oldPassword, String newPassword) {
        return ResponseEntity.ok(userService.changePassword(userId, oldPassword, newPassword));
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
