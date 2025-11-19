package dev.ivantolkach.kanban.KanbanBoardServer.application.service.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
