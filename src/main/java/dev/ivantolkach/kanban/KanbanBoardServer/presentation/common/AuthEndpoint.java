package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignInRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignUpRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.JwtAuthenticationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthEndpoint {
    @PostMapping(ApiEndpoints.Authentication.SIGN_UP)
    JwtAuthenticationResponse signUp(
            @RequestBody @Valid SignUpRequest request
    );

    @PostMapping(ApiEndpoints.Authentication.SIGN_IN)
    JwtAuthenticationResponse signIn(
            @RequestBody @Valid SignInRequest request
    );
}
