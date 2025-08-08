package dev.ivantolkach.kanban.KanbanBoardServer.domain.common.validator;

public class OpacityValidator {
    public static boolean isValidOpacity(String opacity) {
        if (opacity == null) {
            return false;
        }
        try {
            double value = Double.parseDouble(opacity);
            return value >= 0.0 && value <= 1.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
