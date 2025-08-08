package dev.ivantolkach.kanban.KanbanBoardServer.domain.common.validator;

import java.util.Set;

public class ShapeValidator {
    private static final Set<String> VALID_SHAPES = Set.of("circle", "square", "triangle", "rectangle");

    public static boolean isValidShape(String shape) {
        if (shape == null) {
            return false;
        }
        return VALID_SHAPES.contains(shape.toLowerCase());
    }
}
