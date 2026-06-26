package com.kaffah.tasklite.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationHelper {
    public static final String CHANNEL_ID = "task_reminders";

    private NotificationHelper() {
    }

    public static void createReminderChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Pengingat Tugas",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Pengingat deadline tugas");
        manager.createNotificationChannel(channel);
    }

    public static void showTaskReminder(Context context, long taskId) {
        createReminderChannel(context);
        // Full notification content and actions will be implemented with reminder permission handling.
    }
}
