package com.elderwatch.client.services;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.elderwatch.client.DashboardActivity;
import com.elderwatch.client.R;

import org.checkerframework.checker.units.qual.A;

public class NotificationReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_TEXT = "notification_text";

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        String channelId = "myAppNotificationChannel";

        String msg = intent.getStringExtra("msg");
        Intent notifyIntent = new Intent(context, DashboardActivity.class);
        notifyIntent.putExtra("clearSafeZoneNotif", true);
        notifyIntent.putExtra("clearDangerZoneNotif", true);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.elder)
                .setContentTitle("Elder Watch")
                .setContentText(msg)
                .setSound(defaultSoundUri);
        builder.setContentIntent(notifyPendingIntent);
        NotificationChannel notificationChannel = null;
        notificationChannel = new NotificationChannel(channelId, "Notify", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(NOTIFICATION_ID, builder.build());



    }
}
