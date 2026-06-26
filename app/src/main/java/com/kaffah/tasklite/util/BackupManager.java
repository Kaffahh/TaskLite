package com.kaffah.tasklite.util;

import android.content.Context;

public class BackupManager {
    private final Context context;

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public Context getContext() {
        return context;
    }
}
