package fr.accentweb.speedMath.notifications;

import android.content.Context;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleDailyReminder(Context context) {

        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

      //  target.set(Calendar.HOUR_OF_DAY, 18);
       // target.set(Calendar.MINUTE, 0);
       // target.set(Calendar.SECOND, 0);
        target.add(Calendar.MINUTE, 1);

        if (target.getTimeInMillis() <= now.getTimeInMillis()) {
        //    target.add(Calendar.DAY_OF_YEAR, 1);
            target.add(Calendar.MINUTE, 1);

        }

        long delay = target.getTimeInMillis() - now.getTimeInMillis();

        Log.d("NOTIF", "Next notification in " + (delay / 1000) + " seconds");

        OneTimeWorkRequest work =
                new OneTimeWorkRequest.Builder(DailyNotificationWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "daily_notification",
                        ExistingWorkPolicy.REPLACE,
                        work
                );
    }



    public static void cancelDailyReminder(Context context) {
        WorkManager.getInstance(context)
                .cancelUniqueWork("daily_notification");
    }
}
