package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.*;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.UserEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public UserDTOOutput createUpdateUser(UserDTOInput user) {
        return userService.createUpdateUser(user);
    }

    @Override
    public UserDTOOutput changeUserPassword(UUID userId, String oldPassword, String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(UUID userId) {
        userService.deleteUser(userId);
    }
}
