package com.elderwatch.client.services;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.elderwatch.client.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageReceiver extends FirebaseMessagingService {

    @SuppressLint("MissingPermission")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, "channel_id")
                            .setContentTitle(title)
                            .setContentText(body)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}
