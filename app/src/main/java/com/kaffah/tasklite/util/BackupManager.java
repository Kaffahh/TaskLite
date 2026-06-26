package com.kaffah.tasklite.util;

import android.content.Context;

import com.kaffah.tasklite.data.CategoryRepository;
import com.kaffah.tasklite.data.TaskRepository;
import com.kaffah.tasklite.model.Category;
import com.kaffah.tasklite.model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {
    private final Context context;

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public Context getContext() {
        return context;
    }

    public String createBackupJson() throws JSONException {
        CategoryRepository categoryRepository = new CategoryRepository(context);
        TaskRepository taskRepository = new TaskRepository(context);
        PreferenceManager preferenceManager = new PreferenceManager(context);

        List<Category> categories = categoryRepository.getAllCategories();
        List<Task> tasks = taskRepository.getAllTasksForBackup();

        JSONObject root = new JSONObject();
        root.put("application", "TaskLite");
        root.put("schemaVersion", 1);
        root.put("exportedAt", System.currentTimeMillis());
        root.put("categories", categoriesToJson(categories));
        root.put("tasks", tasksToJson(tasks));

        JSONObject preferences = new JSONObject();
        preferences.put("theme", preferenceManager.getThemeMode());
        preferences.put("defaultSort", preferenceManager.getDefaultSort());
        preferences.put("showCompletedInAll", preferenceManager.showCompletedInAll());
        root.put("preferences", preferences);
        return root.toString(2);
    }

    public String defaultBackupFileName() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return "tasklite-backup-" + date + ".json";
    }

    private JSONArray categoriesToJson(List<Category> categories) throws JSONException {
        JSONArray array = new JSONArray();
        for (Category category : categories) {
            JSONObject item = new JSONObject();
            item.put("id", category.id);
            item.put("name", category.name);
            item.put("color", category.color);
            item.put("createdAt", category.createdAt);
            item.put("updatedAt", category.updatedAt);
            array.put(item);
        }
        return array;
    }

    private JSONArray tasksToJson(List<Task> tasks) throws JSONException {
        JSONArray array = new JSONArray();
        for (Task task : tasks) {
            JSONObject item = new JSONObject();
            item.put("id", task.id);
            item.put("title", task.title);
            item.put("description", task.description);
            item.put("categoryId", nullable(task.categoryId));
            item.put("priority", task.priority);
            item.put("dueAt", nullable(task.dueAt));
            item.put("reminderEnabled", task.reminderEnabled);
            item.put("reminderAt", nullable(task.reminderAt));
            item.put("isCompleted", task.completed);
            item.put("completedAt", nullable(task.completedAt));
            item.put("createdAt", task.createdAt);
            item.put("updatedAt", task.updatedAt);
            array.put(item);
        }
        return array;
    }

    private Object nullable(Long value) {
        return value == null ? JSONObject.NULL : value;
    }
}
