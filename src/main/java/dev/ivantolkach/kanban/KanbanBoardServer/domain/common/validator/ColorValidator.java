package dev.ivantolkach.kanban.KanbanBoardServer.domain.common.validator;

public class ColorValidator {
    public static boolean isValidColor(String color) {
        if (color == null) {
            return false;
        }
        return color.matches("^#[0-9A-Fa-f]{6}$") || color.matches("^rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\)$");
    }
}
