package com.elderwatch.client.services;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.elderwatch.client.DashboardActivity;
import com.elderwatch.client.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NotificationReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_TEXT = "notification_text";

    // ---------------------------------------------------------------
    // TODO: Paste your Semaphore API key here
    // ---------------------------------------------------------------
    private static final String SEMAPHORE_API_KEY = "14a9a0daad95c7f0968bcebe99229fce";
    // ---------------------------------------------------------------

    private static final String SEMAPHORE_URL = "https://api.semaphore.co/api/v4/messages";
    private static final String TAG = "NotificationReceiver";

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        String channelId = "myAppNotificationChannel";

        String msg = intent.getStringExtra("msg");

        // Pass "patientID" as an extra when scheduling/triggering this receiver
        // e.g. intent.putExtra("patientID", patient.getPatientID())
        String patientID = intent.getStringExtra("patientID");

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
        NotificationChannel notificationChannel = new NotificationChannel(
                channelId, "Notify", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Send SMS to the guardian associated with this patient
        if (patientID != null && !patientID.isEmpty()) {
            sendSmsToGuardian(patientID, msg);
        }
    }

    /**
     * Step 1: Query patient_guardian collection by patientID to get the guardian's userID.
     */
    private void sendSmsToGuardian(String patientID, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(FSRequest.PG_COLLECTION)
                .whereEqualTo("patientID", patientID)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String guardianUserID = doc.getString("userID");
                            if (guardianUserID != null && !guardianUserID.isEmpty()) {
                                fetchGuardianPhoneAndSendSms(guardianUserID, message);
                            }
                        }
                    } else {
                        Log.w(TAG, "No patient_guardian record found for patientID: " + patientID);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error querying patient_guardian: " + e.getMessage()));
    }

    /**
     * Step 2: Fetch the guardian's phoneNumber from the users collection using their userID.
     */
    private void fetchGuardianPhoneAndSendSms(String userID, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String phoneNumber = userDoc.getString("phoneNumber");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            // Run network call on a background thread
                            new Thread(() -> sendSmsSemaphore(phoneNumber, message)).start();
                        } else {
                            Log.e(TAG, "No phoneNumber found for userID: " + userID);
                        }
                    } else {
                        Log.e(TAG, "User document not found for userID: " + userID);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error fetching user document: " + e.getMessage()));
    }

    /**
     * Step 3: POST to Semaphore API to send the SMS.
     * Always called from a background thread.
     */
    private void sendSmsSemaphore(String phoneNumber, String message) {
        try {
            URL url = new URL(SEMAPHORE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            String params = "apikey=" + URLEncoder.encode(SEMAPHORE_API_KEY, "UTF-8")
                    + "&number=" + URLEncoder.encode(phoneNumber, "UTF-8")
                    + "&message=" + URLEncoder.encode(message, "UTF-8")
                    + "&sendername=ElderWatch";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Semaphore SMS sent to " + phoneNumber + " | HTTP " + responseCode);
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Failed to send Semaphore SMS: " + e.getMessage());
        }
    }
}
