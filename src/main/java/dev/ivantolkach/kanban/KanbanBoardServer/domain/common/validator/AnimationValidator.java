package dev.ivantolkach.kanban.KanbanBoardServer.domain.common.validator;

import java.util.Set;

public class AnimationValidator {
    private static final Set<String> VALID_ANIMATIONS = Set.of("blink", "fade", "slide", "pulse");

    public static boolean isValidAnimation(String animation) {
        if (animation == null) {
            return false;
        }
        return VALID_ANIMATIONS.contains(animation.toLowerCase());
    }
}
