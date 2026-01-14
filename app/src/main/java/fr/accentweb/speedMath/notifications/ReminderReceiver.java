package fr.accentweb.speedMath.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.showDailyNotification(context);

        // Tomorrow
        NotificationScheduler.scheduleDailyReminder(context);
    }
}
