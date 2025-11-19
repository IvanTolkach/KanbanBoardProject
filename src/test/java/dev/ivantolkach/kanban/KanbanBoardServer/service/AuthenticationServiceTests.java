package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignInRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.authentication.SignUpRequest;
import dev.ivantolkach.kanban.KanbanBoardServer.application.dto.user.UserDTOInput;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.AuthenticationService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.JwtService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.UserService;
import dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception.UnauthorizedException;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.JwtAuthenticationResponse;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.mapper.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {
    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private SignUpRequest signUpRequest;
    private SignInRequest signInRequest;
    private User user;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setFname("First");
        signUpRequest.setSname("Second");
        signUpRequest.setLname("Last");
        signUpRequest.setPosition("Position");

        signInRequest = new SignInRequest();
        signInRequest.setEmail("test@example.com");
        signInRequest.setPassword("password");

        UserDTOInput userDTOInput = new UserDTOInput();
        userDTOInput.setEmail("test@example.com");
        userDTOInput.setPassword("password");
        userDTOInput.setFname("First");
        userDTOInput.setSname("Second");
        userDTOInput.setLname("Last");
        userDTOInput.setPosition("Position");
        userDTOInput.setRole(UserRole.ROLE_CLIENT);

        user = mock(User.class);
        jwtToken = "jwt-token";
    }

    @Test
    void signUp_success() {
        when(userService.createUpdateUser(any(UserDTOInput.class))).thenReturn(null);

        when(userMapper.toUser(any(UserDTOInput.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        JwtAuthenticationResponse response = authenticationService.signUp(signUpRequest);

        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());

        verify(userService).createUpdateUser(argThat(dto ->
                dto.getEmail().equals("test@example.com") &&
                        dto.getPassword().equals("password") &&
                        dto.getFname().equals("First") &&
                        dto.getSname().equals("Second") &&
                        dto.getLname().equals("Last") &&
                        dto.getPosition().equals("Position") &&
                        dto.getRole() == UserRole.ROLE_CLIENT
        ));
        verify(userMapper).toUser(any(UserDTOInput.class));
        verify(jwtService).generateToken(user);
        verifyNoMoreInteractions(userService, userMapper, jwtService);
    }

    @Test
    void signIn_success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(userService.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(eq("test@example.com"))).thenReturn(user);

        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        JwtAuthenticationResponse response = authenticationService.signIn(signInRequest);

        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());

        verify(authenticationManager).authenticate(argThat(token ->
                token.getPrincipal().equals("test@example.com") &&
                        token.getCredentials().equals("password")
        ));
        verify(userService).userDetailsService();
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).generateToken(user);
        verifyNoMoreInteractions(authenticationManager, userService, jwtService);
    }

    @Test
    void signIn_badCredentials_throwsUnauthorizedException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authenticationService.signIn(signInRequest));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(authenticationManager).authenticate(argThat(token ->
                token.getPrincipal().equals("test@example.com") &&
                        token.getCredentials().equals("password")
        ));
        verifyNoInteractions(userService, jwtService, userMapper);
    }

    @Test
    void signIn_otherException_throwsUnauthorizedException() {
        RuntimeException otherException = new RuntimeException("Other error");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(otherException);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authenticationService.signIn(signInRequest));
        assertEquals("Other error", exception.getMessage());

        verify(authenticationManager).authenticate(argThat(token ->
                token.getPrincipal().equals("test@example.com") &&
                        token.getCredentials().equals("password")
        ));
        verifyNoInteractions(userService, jwtService, userMapper);
    }
}
