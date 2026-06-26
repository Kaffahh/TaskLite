package com.kaffah.tasklite;

import android.app.Application;

import com.kaffah.tasklite.util.NotificationHelper;

public class TaskLiteApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createReminderChannel(this);
    }
}
