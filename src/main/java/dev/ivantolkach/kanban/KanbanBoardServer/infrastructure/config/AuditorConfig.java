package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.config;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditorConfig {

    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User currentUser) {
                return Optional.of(currentUser);
            }

            return Optional.empty();
        };
    }
}
