package dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignInRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignUpRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.AuthenticationService;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.JwtAuthenticationResponse;
import dev.ivantolkach.kanban.KanbanBoardServer.presentation.common.AuthEndpoint;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthEndpoint {
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Override
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}
