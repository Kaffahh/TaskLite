package com.kaffah.tasklite.data;

public final class DatabaseContract {
    public static final String DATABASE_NAME = "tasklite.db";
    public static final int DATABASE_VERSION = 1;

    private DatabaseContract() {
    }

    public static final class Tasks {
        public static final String TABLE = "tasks";
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String CATEGORY_ID = "category_id";
        public static final String PRIORITY = "priority";
        public static final String DUE_AT = "due_at";
        public static final String REMINDER_ENABLED = "reminder_enabled";
        public static final String REMINDER_AT = "reminder_at";
        public static final String IS_COMPLETED = "is_completed";
        public static final String COMPLETED_AT = "completed_at";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    public static final class Categories {
        public static final String TABLE = "categories";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String COLOR = "color";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }
}
