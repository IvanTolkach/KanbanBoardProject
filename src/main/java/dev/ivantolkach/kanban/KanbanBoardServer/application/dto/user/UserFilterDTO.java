package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFilterDTO {

    private UUID id;

    @NotEmpty(message = "First name cannot be empty")
    private String fname;

    @NotEmpty(message = "Surname cannot be empty")
    private String sname;

    private String lname;

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @Enumerated(EnumType.ORDINAL)
    private UserRole role;

    private LocalDate birthDateFrom;

    private LocalDate birthDateTo;

    @NotEmpty(message = "Position cannot be empty")
    private String position;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;

    @CreatedBy
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    private LocalDateTime createdAtFrom;

    private LocalDateTime createdAtTo;

    private LocalDateTime updatedAtFrom;

    private LocalDateTime updatedAtTo;
}
