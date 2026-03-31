package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOOutput {
    private UUID id;

    @NotEmpty(message = "First name cannot be empty")
    private String fname;

    @NotEmpty(message = "Surname cannot be empty")
    private String sname;

    private String lname;

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @Size(min = 8, message = "Password must include at least 8 symbols")
    private String password;

    @Enumerated(EnumType.ORDINAL)
    private UserRole role;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;

    private LocalDate birthDate;

    @NotEmpty(message = "Position cannot be empty")
    private String position;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
