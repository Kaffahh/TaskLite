package com.kaffah.tasklite.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kaffah.tasklite.model.Task;

public class AlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.context = context.getApplicationContext();
        alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleTaskReminder(Task task) {
        if (task == null || !task.reminderEnabled || task.reminderAt == null) {
            return;
        }
        Intent intent = ReminderReceiver.createIntent(context, task.id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(task.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, task.reminderAt, pendingIntent);
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderAt, pendingIntent);
        }
    }

    public void cancelTaskReminder(long taskId) {
        Intent intent = ReminderReceiver.createIntent(context, taskId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(taskId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    public void rescheduleAllReminders() {
        // Repository-backed rescheduling will be wired when reminder UI is implemented.
    }

    private int requestCode(long taskId) {
        return (int) (taskId % Integer.MAX_VALUE);
    }
}
