package dev.ivantolkach.kanban.KanbanBoardServer.domain.common.validator;

public class SizeValidator {
    public static boolean isValidSize(String size) {
        if (size == null) {
            return false;
        }
        try {
            int value = Integer.parseInt(size);
            return value >= 0 && value <= 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
