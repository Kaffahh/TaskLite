package com.kaffah.tasklite.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String FILE_NAME = "tasklite_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_DEFAULT_SORT = "default_sort";
    private static final String KEY_SHOW_COMPLETED_IN_ALL = "show_completed_in_all";
    private static final String KEY_DEFAULT_REMINDER_OFFSET = "default_reminder_offset";

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getThemeMode() {
        return preferences.getString(KEY_THEME_MODE, "system");
    }

    public String getDefaultSort() {
        return preferences.getString(KEY_DEFAULT_SORT, "due_asc");
    }

    public boolean showCompletedInAll() {
        return preferences.getBoolean(KEY_SHOW_COMPLETED_IN_ALL, false);
    }

    public long getDefaultReminderOffset() {
        return preferences.getLong(KEY_DEFAULT_REMINDER_OFFSET, 0L);
    }
}
