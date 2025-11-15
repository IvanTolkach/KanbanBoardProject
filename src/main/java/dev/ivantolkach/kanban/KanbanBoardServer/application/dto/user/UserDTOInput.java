package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.EntityStatus;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOInput {
    private UUID id;

    private String fname;

    private String sname;

    private String lname;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must include at least 8 symbols")
    @Size(max = 255, message = "The password length must be no more than 255 characters")
    private String password;

    @Enumerated(EnumType.ORDINAL)
    private UserRole role;

    @Enumerated(EnumType.ORDINAL)
    private EntityStatus status;

    private LocalDate birthDate;

    private String position;
}
