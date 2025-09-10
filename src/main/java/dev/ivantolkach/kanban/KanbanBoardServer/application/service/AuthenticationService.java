package dev.ivantolkach.kanban.KanbanBoardServer.application.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignInRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignUpRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.JwtAuthenticationResponse;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var userDto = new UserDTOInput();
        userDto.setEmail(request.getEmail());
        userDto.setPassword(passwordEncoder.encode(request.getPassword()));
        userDto.setFname(request.getFname());
        userDto.setSname(request.getSname());
        userDto.setLname(request.getLname());
        userDto.setPosition(request.getPosition());
        userDto.setRole(UserRole.ROLE_CLIENT);

        userService.createUpdateUser(userDto);

        User user = userMapper.toUser(userDto);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            ));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw new UnauthorizedException("Invalid email or password");
            }
            throw new UnauthorizedException(e.getMessage());
        }

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getEmail());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
