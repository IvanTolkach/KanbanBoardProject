package dev.ivantolkach.kanban.KanbanBoardServer.presentation.common;

public final class ApiEndpoints {

    public static final String API_BASE = "/api";

    public static class Authentication {
        public static final String BASE = API_BASE + "/auth";
        public static final String SIGN_UP = BASE + "/sign-up";
        public static final String SIGN_IN = BASE + "/sign-in";
    }

    public static class Project {
        public static final String BASE = API_BASE + "/projects";
        public static final String BY_ID = BASE + "/{projectId}";
    }

    public static class ProjectColumn {
        public static final String BASE = API_BASE + "/columns";
        public static final String BY_ID = BASE + "/{columnId}";
        public static final String BY_PROJECT_ID = Project.BY_ID + "/columns";
    }

    public static class Task {
        public static final String BASE = API_BASE + "/tasks";
        public static final String BY_ID = BASE + "/{taskId}";
        public static final String BY_PROJECT_ID = Project.BY_ID + "/tasks";
        public static final String USERS_TASKS = API_BASE + "/usersTasks";
        public static final String ATTACHED_USERS = BY_ID + "/users";
        public static final String ATTACH_USER = ATTACHED_USERS + "/{userId}";
    }

    public static class Tag {
        public static final String BASE = API_BASE + "/tags";
        public static final String BY_ID = BASE + "/{tagId}";
        public static final String TASKS_TAGS = API_BASE + "/tasksTags";
        public static final String TASKS_TAGS_BY_ID = TASKS_TAGS + "/{taskTagId}";
        public static final String ATTACH_TAG = Task.BY_ID + "/tags/{tagId}";
    }

    public static class User {
        public static final String BASE = API_BASE + "/users";
        public static final String BY_ID = BASE + "/{userId}";
        public static final String CHANGE_PASSWORD = BY_ID + "/password";
    }

    public static class Document {
        public static final String BASE = API_BASE + "/documents";
        public static final String BY_ID = BASE + "/{documentId}";
        public static final String UPLOAD_UPDATE = Task.BY_ID + "/documents";
    }
}
