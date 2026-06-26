package com.kaffah.tasklite.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaffah.tasklite.model.Task;
import com.kaffah.tasklite.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    public static final String FILTER_ALL = "all";
    public static final String FILTER_TODAY = "today";
    public static final String FILTER_OVERDUE = "overdue";
    public static final String FILTER_COMPLETED = "completed";

    private final DatabaseHelper databaseHelper;

    public TaskRepository(Context context) {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public List<Task> getTasks(String filter, String searchQuery) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<String> args = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (FILTER_COMPLETED.equals(filter)) {
            where.append("tasks.is_completed = 1");
        } else if (FILTER_TODAY.equals(filter)) {
            where.append("tasks.is_completed = 0 AND tasks.due_at >= ? AND tasks.due_at < ?");
            args.add(String.valueOf(DateTimeUtils.startOfToday()));
            args.add(String.valueOf(DateTimeUtils.startOfTomorrow()));
        } else if (FILTER_OVERDUE.equals(filter)) {
            where.append("tasks.is_completed = 0 AND tasks.due_at IS NOT NULL AND tasks.due_at < ?");
            args.add(String.valueOf(System.currentTimeMillis()));
        } else {
            where.append("tasks.is_completed = 0");
        }

        String query = searchQuery == null ? "" : searchQuery.trim();
        if (!query.isEmpty()) {
            String like = "%" + escapeLike(query) + "%";
            where.append(" AND (tasks.title LIKE ? ESCAPE '\\' OR tasks.description LIKE ? ESCAPE '\\' OR categories.name LIKE ? ESCAPE '\\')");
            args.add(like);
            args.add(like);
            args.add(like);
        }

        String sql = "SELECT tasks.*, categories.name AS category_name, categories.color AS category_color "
                + "FROM tasks LEFT JOIN categories ON categories.id = tasks.category_id "
                + "WHERE " + where
                + " ORDER BY CASE WHEN tasks.due_at IS NULL THEN 1 ELSE 0 END, "
                + "tasks.due_at ASC, tasks.priority DESC, tasks.created_at DESC";

        List<Task> tasks = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                tasks.add(readTask(cursor));
            }
        }
        return tasks;
    }

    public int[] getSummary() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int active = countByCompleted(db, false);
        int completed = countByCompleted(db, true);
        return new int[]{active, completed};
    }

    public Task getTask(long taskId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql = "SELECT tasks.*, categories.name AS category_name, categories.color AS category_color "
                + "FROM tasks LEFT JOIN categories ON categories.id = tasks.category_id "
                + "WHERE tasks.id = ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(taskId)})) {
            if (cursor.moveToFirst()) {
                return readTask(cursor);
            }
        }
        return null;
    }

    public long insert(Task task) {
        long now = System.currentTimeMillis();
        task.createdAt = now;
        task.updatedAt = now;
        return databaseHelper.getWritableDatabase().insertOrThrow(
                DatabaseContract.Tasks.TABLE,
                null,
                toContentValues(task, true));
    }

    public int update(Task task) {
        task.updatedAt = System.currentTimeMillis();
        return databaseHelper.getWritableDatabase().update(
                DatabaseContract.Tasks.TABLE,
                toContentValues(task, false),
                DatabaseContract.Tasks.ID + " = ?",
                new String[]{String.valueOf(task.id)});
    }

    public int setCompleted(long taskId, boolean completed) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Tasks.IS_COMPLETED, completed ? 1 : 0);
        values.put(DatabaseContract.Tasks.COMPLETED_AT, completed ? System.currentTimeMillis() : null);
        values.put(DatabaseContract.Tasks.REMINDER_ENABLED, 0);
        values.putNull(DatabaseContract.Tasks.REMINDER_AT);
        values.put(DatabaseContract.Tasks.UPDATED_AT, System.currentTimeMillis());
        return databaseHelper.getWritableDatabase().update(
                DatabaseContract.Tasks.TABLE,
                values,
                DatabaseContract.Tasks.ID + " = ?",
                new String[]{String.valueOf(taskId)});
    }

    public int delete(long taskId) {
        return databaseHelper.getWritableDatabase().delete(
                DatabaseContract.Tasks.TABLE,
                DatabaseContract.Tasks.ID + " = ?",
                new String[]{String.valueOf(taskId)});
    }

    public int deleteCompleted() {
        return databaseHelper.getWritableDatabase().delete(
                DatabaseContract.Tasks.TABLE,
                DatabaseContract.Tasks.IS_COMPLETED + " = ?",
                new String[]{"1"});
    }

    private int countByCompleted(SQLiteDatabase db, boolean completed) {
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE is_completed = ?",
                new String[]{completed ? "1" : "0"})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private ContentValues toContentValues(Task task, boolean includeCreatedAt) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Tasks.TITLE, task.title.trim());
        values.put(DatabaseContract.Tasks.DESCRIPTION, task.description == null ? "" : task.description.trim());
        if (task.categoryId == null) {
            values.putNull(DatabaseContract.Tasks.CATEGORY_ID);
        } else {
            values.put(DatabaseContract.Tasks.CATEGORY_ID, task.categoryId);
        }
        values.put(DatabaseContract.Tasks.PRIORITY, task.priority);
        putLongOrNull(values, DatabaseContract.Tasks.DUE_AT, task.dueAt);
        values.put(DatabaseContract.Tasks.REMINDER_ENABLED, task.reminderEnabled ? 1 : 0);
        putLongOrNull(values, DatabaseContract.Tasks.REMINDER_AT, task.reminderAt);
        values.put(DatabaseContract.Tasks.IS_COMPLETED, task.completed ? 1 : 0);
        putLongOrNull(values, DatabaseContract.Tasks.COMPLETED_AT, task.completedAt);
        if (includeCreatedAt) {
            values.put(DatabaseContract.Tasks.CREATED_AT, task.createdAt);
        }
        values.put(DatabaseContract.Tasks.UPDATED_AT, task.updatedAt);
        return values;
    }

    private void putLongOrNull(ContentValues values, String key, Long value) {
        if (value == null) {
            values.putNull(key);
        } else {
            values.put(key, value);
        }
    }

    private Task readTask(Cursor cursor) {
        Task task = new Task();
        task.id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.ID));
        task.title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.TITLE));
        task.description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.DESCRIPTION));
        task.categoryId = getNullableLong(cursor, DatabaseContract.Tasks.CATEGORY_ID);
        task.priority = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.PRIORITY));
        task.dueAt = getNullableLong(cursor, DatabaseContract.Tasks.DUE_AT);
        task.reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.REMINDER_ENABLED)) == 1;
        task.reminderAt = getNullableLong(cursor, DatabaseContract.Tasks.REMINDER_AT);
        task.completed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.IS_COMPLETED)) == 1;
        task.completedAt = getNullableLong(cursor, DatabaseContract.Tasks.COMPLETED_AT);
        task.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.CREATED_AT));
        task.updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Tasks.UPDATED_AT));
        int categoryNameIndex = cursor.getColumnIndex("category_name");
        task.categoryName = categoryNameIndex >= 0 ? cursor.getString(categoryNameIndex) : null;
        int categoryColorIndex = cursor.getColumnIndex("category_color");
        task.categoryColor = categoryColorIndex >= 0 && !cursor.isNull(categoryColorIndex)
                ? cursor.getInt(categoryColorIndex)
                : 0;
        return task;
    }

    private Long getNullableLong(Cursor cursor, String column) {
        int index = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(index) ? null : cursor.getLong(index);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
