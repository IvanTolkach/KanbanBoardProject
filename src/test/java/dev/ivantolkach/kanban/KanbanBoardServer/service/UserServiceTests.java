package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserService;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserListMapper userListMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void getUsersByFilter_returnsFilteredUsers() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setEmail("billy.herrington@example.com");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("billy.herrington@example.com");
        List<User> users = List.of(user);
        List<UserDTOOutput> expectedDTOs = List.of(new UserDTOOutput());

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);
        when(userListMapper.toDTOList(users)).thenReturn(expectedDTOs);

        List<UserDTOOutput> result = userService.getUsersByFilter(filter);

        assertEquals(expectedDTOs, result);
        assertEquals(1, result.size());
        verify(userRepository).findAll(any(Specification.class));
        verify(userListMapper).toDTOList(users);
        verifyNoMoreInteractions(userRepository, userListMapper, userMapper, passwordEncoder);
    }

    @Test
    void getUsersByFilter_noMatches_returnsEmptyList() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setEmail("billy.herrington@example.com");

        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(userListMapper.toDTOList(List.of())).thenReturn(List.of());

        List<UserDTOOutput> result = userService.getUsersByFilter(filter);

        assertTrue(result.isEmpty());
        verify(userRepository).findAll(any(Specification.class));
        verify(userListMapper).toDTOList(List.of());
        verifyNoMoreInteractions(userRepository, userListMapper, userMapper, passwordEncoder);
    }

    @Test
    void createUser_success() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("Herrington");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("password123");
        input.setPosition("Developer");
        input.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFname("Billy");
        user.setSname("Herrington");
        user.setEmail("billy.herrington@example.com");
        user.setPassword("password123");
        user.setPosition("Developer");
        user.setRole(UserRole.ROLE_CLIENT);
        user.setStatus(EntityStatus.ACTIVE);
        UserDTOOutput expectedDTO = new UserDTOOutput();

        when(userRepository.findByEmail("billy.herrington@example.com")).thenReturn(Optional.empty());
        when(userMapper.toUser(input)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setPassword("hashedPassword");
            return savedUser;
        });
        when(userMapper.toDTO(user)).thenReturn(expectedDTO);

        UserDTOOutput result = userService.createUpdateUser(input);

        assertEquals(expectedDTO, result);
        assertEquals(UserRole.ROLE_CLIENT, user.getRole());
        assertEquals("hashedPassword", user.getPassword());
        verify(userRepository).findByEmail("billy.herrington@example.com");
        verify(userMapper).toUser(input);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
        verify(userMapper).toDTO(user);
    }

    @Test
    void createUser_emptyFirstName_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("");
        input.setSname("Herrington");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("password123");
        input.setPosition("Developer");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("First name cannot be empty", ex.getMessage());
        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void createUser_emptySurname_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("password123");
        input.setPosition("Developer");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("Surname cannot be empty", ex.getMessage());
        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void createUser_emptyEmail_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("Herrington");
        input.setEmail("");
        input.setPassword("password123");
        input.setPosition("Developer");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("Email cannot be empty", ex.getMessage());
        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void createUser_shortPassword_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("Herrington");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("short");
        input.setPosition("Developer");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("Password cannot be empty or shorter than 8 characters", ex.getMessage());
        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void createUser_emptyPosition_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("Herrington");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("password123");
        input.setPosition("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("Position cannot be empty", ex.getMessage());
        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void createUser_emailExists_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("Herrington");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("password123");
        input.setPosition("Developer");
        User existingUser = new User();
        existingUser.setEmail("billy.herrington@example.com");

        when(userRepository.findByEmail("billy.herrington@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("User with the same email already exist", ex.getMessage());
        verify(userRepository).findByEmail("billy.herrington@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper, passwordEncoder);
    }

    @Test
    void createUser_closedStatus_throwsIllegalArgumentException() {
        UserDTOInput input = new UserDTOInput();
        input.setId(null);
        input.setFname("Billy");
        input.setSname("Herrington");
        input.setEmail("billy.herrington@example.com");
        input.setPassword("password123");
        input.setPosition("Developer");
        input.setStatus(EntityStatus.CLOSED);

        when(userRepository.findByEmail("billy.herrington@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("User status cannot be CLOSED", ex.getMessage());
        verify(userRepository).findByEmail("billy.herrington@example.com");
        verifyNoInteractions(userMapper, passwordEncoder);
    }

    @Test
    void updateUser_success() {
        UUID userId = UUID.randomUUID();
        UserDTOInput input = new UserDTOInput();
        input.setId(userId);
        input.setFname("Ivan");
        input.setSname("Barin");
        input.setLname("Middle");
        input.setPosition("Manager");
        input.setBirthDate(LocalDate.of(1990, 1, 1));
        input.setStatus(EntityStatus.ACTIVE);
        User user = new User();
        user.setId(userId);
        user.setFname("Billy");
        user.setSname("Herrington");
        user.setLname("Old");
        user.setPosition("Developer");
        user.setBirthDate(LocalDate.of(1985, 1, 1));
        user.setStatus(EntityStatus.ACTIVE);
        UserDTOOutput expectedDTO = new UserDTOOutput();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedDTO);

        UserDTOOutput result = userService.createUpdateUser(input);

        assertEquals(expectedDTO, result);
        assertEquals("Ivan", user.getFname());
        assertEquals("Barin", user.getSname());
        assertEquals("Middle", user.getLname());
        assertEquals("Manager", user.getPosition());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthDate());
        assertEquals(EntityStatus.ACTIVE, user.getStatus());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(userMapper).toDTO(user);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUser_userNotFound_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UserDTOInput input = new UserDTOInput();
        input.setId(userId);
        input.setFname("Ivan");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("User not found with id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void updateUser_restrictedUserNotActive_throwsIllegalStateException() {
        UUID userId = UUID.randomUUID();
        UserDTOInput input = new UserDTOInput();
        input.setId(userId);
        input.setFname("Ivan");
        input.setStatus(EntityStatus.RESTRICTED);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.RESTRICTED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("Cannot update restricted user. User id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void updateUser_nullFields_doesNotChangeFields() {
        UUID userId = UUID.randomUUID();
        UserDTOInput input = new UserDTOInput();
        input.setId(userId);
        input.setFname(null);
        input.setSname("");
        User user = new User();
        user.setId(userId);
        user.setFname("Billy");
        user.setSname("Herrington");
        UserDTOOutput expectedDTO = new UserDTOOutput();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedDTO);

        UserDTOOutput result = userService.createUpdateUser(input);

        assertEquals(expectedDTO, result);
        assertEquals("Billy", user.getFname());
        assertEquals("Herrington", user.getSname());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(userMapper).toDTO(user);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUser_createdStatus_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UserDTOInput input = new UserDTOInput();
        input.setId(userId);
        input.setStatus(EntityStatus.CREATED);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("User status cannot be changed back to CREATED", ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void updateUser_closedStatus_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UserDTOInput input = new UserDTOInput();
        input.setId(userId);
        input.setStatus(EntityStatus.CLOSED);
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUpdateUser(input));

        assertEquals("User status cannot be CLOSED", ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void changePassword_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setPassword("hashedOldPassword");
        String oldPassword = "oldPassword";
        String newPassword = "newPassword123";
        String hashedNewPassword = "hashedNewPassword";
        UserDTOOutput expectedDTO = new UserDTOOutput();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedDTO);

        UserDTOOutput result = userService.changePassword(userId, oldPassword, newPassword);

        assertEquals(expectedDTO, result);
        assertEquals("hashedNewPassword", user.getPassword());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "hashedOldPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        verify(userMapper).toDTO(user);
    }

    @Test
    void changePassword_userNotFound_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        String oldPassword = "oldPassword";
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(userId, oldPassword, newPassword));

        assertEquals("User not found with id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder, userMapper, userListMapper);
    }

    @Test
    void changePassword_incorrectOldPassword_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setPassword("hashedOldPassword");
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, "hashedOldPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(userId, oldPassword, newPassword));

        assertEquals("Incorrect old password", ex.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "hashedOldPassword");
        verifyNoInteractions(userMapper, userListMapper);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_emptyNewPassword_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setPassword("hashedOldPassword");
        String oldPassword = "oldPassword";
        String newPassword = "";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, "hashedOldPassword")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(userId, oldPassword, newPassword));

        assertEquals("New password cannot be empty", ex.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "hashedOldPassword");
        verifyNoInteractions(userMapper, userListMapper);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_shortNewPassword_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setPassword("hashedOldPassword");
        String oldPassword = "oldPassword";
        String newPassword = "short";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, "hashedOldPassword")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(userId, oldPassword, newPassword));

        assertEquals("New password must be at least 8 characters long", ex.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "hashedOldPassword");
        verifyNoInteractions(userMapper, userListMapper);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_createdStatus_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.CREATED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void deleteUser_userNotFound_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId));

        assertEquals("User not found with id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void deleteUser_activeStatus_throwsIllegalStateException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(userId));

        assertEquals("Cannot delete ACTIVE or RESTRICTED user. User id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void deleteUser_restrictedStatus_throwsIllegalStateException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setStatus(EntityStatus.RESTRICTED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(userId));

        assertEquals("Cannot delete ACTIVE or RESTRICTED user. User id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }

    @Test
    void deleteUser_unknownStatus_throwsIllegalStateException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setStatus(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(userId));

        assertEquals("Cant delete user with id: " + userId, ex.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper, userListMapper, passwordEncoder);
    }
}