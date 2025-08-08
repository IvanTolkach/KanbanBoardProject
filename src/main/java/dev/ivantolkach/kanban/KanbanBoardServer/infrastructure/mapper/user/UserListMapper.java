package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface UserListMapper {
    List<UserDTOOutput> toDTOList(List<User> users);

    List<User> toUserList(List<UserDTOInput> userDTOInputs);
}
