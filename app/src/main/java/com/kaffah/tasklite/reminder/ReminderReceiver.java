package com.kaffah.tasklite.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaffah.tasklite.util.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_TASK_ID = "task_id";

    public static Intent createIntent(Context context, long taskId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra(EXTRA_TASK_ID, 0);
        if (taskId > 0) {
            NotificationHelper.showTaskReminder(context, taskId);
        }
    }
}
