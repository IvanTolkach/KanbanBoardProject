package dev.ivantolkach.kanban.KanbanBoardServer.infrastructure.config;

import dev.ivantolkach.kanban.KanbanBoardServer.domain.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditorConfig {

    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> {
            User stubUser = new User();
            stubUser.setId(UUID.fromString("e0178bc3-d477-4900-a89f-58734c4a8d6e"));
            return Optional.of(stubUser);
        };
    }
}
