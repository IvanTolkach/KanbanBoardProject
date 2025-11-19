package dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @Size(max = 255, message = "Email address must be between 5 and 255 characters long")
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Email must follow the format user@example.com")
    private String email;

    @Size(max = 255, message = "The password length must be between 8 and 255 characters")
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
