package com.vanvatcorporation.doubleclips.helper;


import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper  {

    public static final String CHANNEL_ID_GENERAL = "generalChannel";

    public static void sendNotification(Context context, int id, String title, String message) {

        Notification.Builder builderNotification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new Notification.Builder(context, CHANNEL_ID_GENERAL) : new Notification.Builder(context);
        builderNotification = builderNotification
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(context.getApplicationInfo().icon)
                .setColor(0xFFFF0000)
                .setCategory(Notification.CATEGORY_CALL)
                .setPriority(Notification.PRIORITY_MAX);


        // Send the notification to the system
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builderNotification.build());//sbn.getId()
    }


    public static void createNotificationChannel(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }


        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DoubleClips General Notification";
            String description = "just chill...this kind of notifications doesn't need attention";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_GENERAL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);




//            name = "High Priority Notification Channel";
//            description = "Require Attention Immediately!!!";
//            importance = NotificationManager.IMPORTANCE_MAX;
//            channel = new NotificationChannel(CHANNEL_ID_HIGH, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this.
//            notificationManager.createNotificationChannel(channel);
        }
    }
//
//
//    public void PostProgressNotification() {
//        CountDownTimer countDownTimer = new CountDownTimer(60 * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                int progress = (int) (((60 * 1000 - millisUntilFinished) / (60 * 1000.0)) * 100);
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                        .setSmallIcon(getApplicationInfo().icon)
//                        .setContentTitle("Progress Notification")
//                        .setContentText("Time left: " + millisUntilFinished / 1000 + " seconds")
//                        .setProgress(100, progress, false)
//                        .setPriority(NotificationCompat.PRIORITY_LOW)
//                        .setOnlyAlertOnce(true)
//                        .setOngoing(true);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    NotificationManagerCompat.from(getApplicationContext()).notify(100, builder.build());
//                }
//            }
//
//            @Override
//            public void onFinish() {
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                        .setSmallIcon(getApplicationInfo().icon)
//                        .setContentTitle("Progress Notification")
//                        .setContentText("Done!")
//                        .setPriority(NotificationCompat.PRIORITY_LOW);
//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                NotificationManagerCompat.from(getApplicationContext()).notify(100, builder.build());
//            }
//        }.start();}
//


}