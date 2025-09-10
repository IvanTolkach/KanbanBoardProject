package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    @Size(min = 5, max = 255, message = "Email address must be between 5 and 255 characters long")
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Email must follow the format user@example.com")
    private String email;

    @Size(min = 8, message = "Password must include at least 8 symbols")
    @Size(max = 255, message = "The password length must be no more than 255 characters")
    private String password;

    @NotBlank(message = "First name cannot be empty")
    @Size(max = 255, message = "First name length must be no more than 255 characters")
    private String fname;

    @NotBlank(message = "Surname cannot be empty")
    @Size(max = 255, message = "Surname length must be no more than 255 characters")
    private String sname;

    @Size(max = 255, message = "Last name length must be no more than 255 characters")
    private String lname;

    @Column(name = "position", length = 100)
    private String position;
}
