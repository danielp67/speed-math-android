package fr.accentweb.speedMath.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import fr.accentweb.speedMath.MainActivity;
import fr.accentweb.speedMath.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        String title = message.getNotification() != null
                ? message.getNotification().getTitle()
                : "Speed Math";

        String body = message.getNotification() != null
                ? message.getNotification().getBody()
                : "";

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_daily", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "daily_channel")
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this).notify(1001, builder.build());
    }
}

