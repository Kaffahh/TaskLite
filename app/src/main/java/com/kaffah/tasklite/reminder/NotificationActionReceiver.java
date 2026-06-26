package com.kaffah.tasklite.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_COMPLETE = "com.kaffah.tasklite.ACTION_COMPLETE";
    public static final String ACTION_SNOOZE = "com.kaffah.tasklite.ACTION_SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Complete and snooze actions will be connected to TaskRepository in the reminder phase.
    }
}
