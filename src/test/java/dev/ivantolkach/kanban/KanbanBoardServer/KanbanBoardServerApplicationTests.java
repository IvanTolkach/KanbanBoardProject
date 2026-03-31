package dev.ivantolkach.kanban.KanbanBoardServer;

import dev.ivantolkach.kanban.KanbanBoardServer.presentation.rest.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class KanbanBoardServerApplicationTests {

	@Autowired
	private AuthController authController;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ColumnController columnController;

	@Autowired
	private TaskController taskController;

	@Autowired
	private UserController userController;

	@Autowired
	private DocumentController documentController;

	@Autowired
	private TagController tagController;

	@Test
	void contextLoadsAndControllersAreNotNull() {
		assertNotNull(authController, "AuthController should be initialized");
		assertNotNull(projectController, "ProjectController should be initialized");
		assertNotNull(columnController, "ColumnController should be initialized");
		assertNotNull(taskController, "TaskController should be initialized");
		assertNotNull(userController, "UserController should be initialized");
		assertNotNull(documentController, "DocumentController should be initialized");
		assertNotNull(tagController, "TagController should be initialized");
	}
}
