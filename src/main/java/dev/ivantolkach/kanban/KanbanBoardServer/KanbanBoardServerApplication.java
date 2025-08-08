package dev.ivantolkach.kanban.KanbanBoardServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class KanbanBoardServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KanbanBoardServerApplication.class, args);
	}

}
