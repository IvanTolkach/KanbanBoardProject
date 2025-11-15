package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOOutput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserFilterDTO;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserListMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserMapper;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.repository.UserRepository;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.persistence.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserListMapper userListMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    public List<UserDTOOutput> getAllUsers() {
        return userListMapper.toDTOList(userRepository.findAll());
    }

    public List<UserDTOOutput> getUsersByFilter(UserFilterDTO filter) {
        return userListMapper.toDTOList(userRepository.findAll(UserSpecification.filterBy(filter)));
    }

    public Optional<UserDTOOutput> getUserById(UUID userId) {
        return userRepository.findById(userId).map(userMapper::toDTO);
    }

    public UserDTOOutput createUpdateUser(UserDTOInput userDTOInput) {

        User user;

        if (userDTOInput.getId() != null) {
            user = userRepository.findById(userDTOInput.getId())
                    .orElseThrow(()->new IllegalArgumentException("User not found with id: " + userDTOInput.getId()));

            User currentUser = getCurrentUser();

            if (!(currentUser.getRole() == UserRole.ROLE_ADMIN)) {
                if (!(user.getId().equals(currentUser.getId()))) {
                    throw new UnauthorizedException("Not allowed to edit this entity");
                }
            }

            if (user.getStatus() == EntityStatus.RESTRICTED && userDTOInput.getStatus() != EntityStatus.ACTIVE) {
                throw new IllegalStateException("Cannot update restricted user. User id: " + userDTOInput.getId());
            }

            if (!(userDTOInput.getFname() == null || userDTOInput.getFname().isBlank())) {
                user.setFname(userDTOInput.getFname());
            }
            if (!(userDTOInput.getSname() == null || userDTOInput.getSname().isBlank())) {
                user.setSname(userDTOInput.getSname());
            }
            if (!(userDTOInput.getLname() == null || userDTOInput.getLname().isBlank())) {
                user.setLname(userDTOInput.getLname());
            }
            if (!(userDTOInput.getPosition() == null || userDTOInput.getPosition().isBlank())) {
                user.setPosition(userDTOInput.getPosition());
            }
            if (!(userDTOInput.getBirthDate() == null)) {
                user.setBirthDate(userDTOInput.getBirthDate());
            }
            if (!(userDTOInput.getStatus() == null)) {

                if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
                    throw new UnauthorizedException("Not allowed to edit status of this entity");
                }

                if (userDTOInput.getStatus() == EntityStatus.CREATED) {
                    throw new IllegalArgumentException("User status cannot be changed back to CREATED");
                }
                if (userDTOInput.getStatus() == EntityStatus.CLOSED) {
                    throw new IllegalArgumentException("User status cannot be CLOSED");
                }
                if (userDTOInput.getStatus() == EntityStatus.ACTIVE || userDTOInput.getStatus() == EntityStatus.RESTRICTED) {
                    user.setStatus(userDTOInput.getStatus());
                }
            }
        }
        else {
            if (userDTOInput.getFname() == null || userDTOInput.getFname().isBlank()) {
                throw new IllegalArgumentException("First name cannot be empty");
            }
            if (userDTOInput.getSname() == null || userDTOInput.getSname().isBlank()) {
                throw new IllegalArgumentException("Surname cannot be empty");
            }
            if (userDTOInput.getEmail() == null || userDTOInput.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (userDTOInput.getPassword() == null || userDTOInput.getPassword().isBlank() || userDTOInput.getPassword().length() < 8) {
                throw new IllegalArgumentException("Password cannot be empty or shorter than 8 characters");
            }
            if (userDTOInput.getPosition() == null || userDTOInput.getPosition().isBlank()) {
                throw new IllegalArgumentException("Position cannot be empty");
            }
            if (userRepository.findByEmail(userDTOInput.getEmail()).isPresent()) {
                throw new IllegalArgumentException("User with the same email already exist");
            }
            if (userDTOInput.getStatus() == EntityStatus.CLOSED) {
                throw new IllegalArgumentException("User status cannot be CLOSED");
            }

            user = userMapper.toUser(userDTOInput);
            user.setStatus(userDTOInput.getStatus() != null ? userDTOInput.getStatus() : EntityStatus.CREATED);
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setRole(UserRole.ROLE_CLIENT);
            user.setPassword(hashedPassword);
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    public UserDTOOutput changePassword(UUID userId, String oldPassword, String newPassword) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("User not found with id: " + userId));

        if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        String hashedNewPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(hashedNewPassword);

        return userMapper.toDTO(userRepository.save(existingUser));
    }

    public void deleteUser(UUID userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("User not found with id: " + userId));

        if (existingUser.getStatus() == EntityStatus.ACTIVE || existingUser.getStatus() == EntityStatus.RESTRICTED) {
            throw new IllegalStateException("Cannot delete ACTIVE or RESTRICTED user. User id: " + userId);
        }

        if (existingUser.getStatus() == EntityStatus.CREATED) {
            userRepository.delete(existingUser);
            return;
        }

        throw new IllegalStateException("Cant delete user with id: " + userId);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    }

    public UserDetailsService userDetailsService() {
        return this::getByEmail;
    }

    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByEmail(username);
    }
}
