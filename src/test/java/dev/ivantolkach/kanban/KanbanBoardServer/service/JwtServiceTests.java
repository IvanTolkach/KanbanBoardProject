package dev.ivantolkach.kanban.KanbanBoardServer.service;

import dev.ivantolkach.kanban.KanbanBoardServer.application.service.JwtService;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.common.enums.UserRole;
import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTests {
    private JwtService jwtService;
    private final String base64Key = "u3DHtF4XyvA2qQwLnbfku8iHZ2r5/cJ2XcT7Xy6QkS0=";

    private String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(base64Key)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", base64Key);
    }

    @Test
    void testGenerateToken_WithCustomUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@mail.com");
        user.setFname("John");
        user.setSname("Doe");
        user.setRole(UserRole.ROLE_ADMIN);
        user.setPassword("1234");

        String token = jwtService.generateToken(user);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(java.util.Base64.getDecoder().decode(base64Key))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(user.getEmail(), claims.getSubject());

        assertEquals(user.getId().toString(), String.valueOf(claims.get("id")));
        assertEquals(user.getEmail(), String.valueOf(claims.get("email")));
        assertEquals(user.getFname(), String.valueOf(claims.get("fname")));
        assertEquals(user.getSname(), String.valueOf(claims.get("sname")));
        assertEquals(UserRole.ROLE_ADMIN, UserRole.valueOf(claims.get("role").toString()));
    }

    @Test
    void testGenerateToken_WithSimpleUserDetails() {
        var userDetails = mock(org.springframework.security.core.userdetails.User.class);
        when(userDetails.getUsername()).thenReturn("simple");

        String token = jwtService.generateToken(userDetails);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(java.util.Base64.getDecoder().decode(base64Key))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("simple", claims.getSubject());
        assertFalse(claims.containsKey("id"));
        assertFalse(claims.containsKey("email"));
    }

    @Test
    void testExtractUserName() {
        String token = createToken("myUser");

        String username = jwtService.extractUserName(token);

        assertEquals("myUser", username);
    }

    @Test
    void testIsTokenValid_ValidToken() {
        var userDetails = mock(org.springframework.security.core.userdetails.User.class);
        when(userDetails.getUsername()).thenReturn("john");

        String token = createToken("john");

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_InvalidUsername() {
        var userDetails = mock(org.springframework.security.core.userdetails.User.class);
        when(userDetails.getUsername()).thenReturn("john");

        String token = createToken("otherUser");

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_ExpiredToken_Throws() {
        var userDetails = mock(org.springframework.security.core.userdetails.User.class);

        String expiredToken = Jwts.builder()
                .setSubject("john")
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(base64Key)),
                        SignatureAlgorithm.HS256)
                .compact();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, userDetails));

        verifyNoInteractions(userDetails);
    }

    @Test
    void testExtractExpiration() {
        String token = createToken("check");

        Date exp = ReflectionTestUtils.invokeMethod(jwtService, "extractExpiration", token);

        assertNotNull(exp);
        assertTrue(exp.after(new Date()));
    }

    @Test
    void testExtractAllClaims() {
        String token = createToken("userX");

        Claims claims = ReflectionTestUtils.invokeMethod(jwtService, "extractAllClaims", token);

        assertEquals("userX", claims.getSubject());
    }

    @Test
    void testExtractClaim_Generic() {
        String token = createToken("abc");

        String subject = ReflectionTestUtils.invokeMethod(jwtService,
                "extractClaim",
                token,
                (java.util.function.Function<Claims, String>) Claims::getSubject);

        assertEquals("abc", subject);
    }

    @Test
    void testGetSigningKey() {
        Object key = ReflectionTestUtils.invokeMethod(jwtService, "getSigningKey");
        assertNotNull(key);
    }
}
