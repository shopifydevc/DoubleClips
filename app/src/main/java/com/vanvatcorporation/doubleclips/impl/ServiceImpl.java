package com.vanvatcorporation.doubleclips.impl;


import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ServiceImpl extends Service {

    @Override
    public void onCreate() {
        super.onCreate();



//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ExtendedUnityPlayerActivity.class), PendingIntent.FLAG_IMMUTABLE);
//            Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
//            Notification notification = builder
//                    .setContentTitle("Dev Utilities :)")
//                    .setContentText("Running...")
//                    .setSmallIcon(getApplicationInfo().icon)
//                    .setContentIntent(pendingIntent)
//                    .build();
//            startForeground(1100, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //LoggingManager.LogToNoteOverlay(this, "Destroy the start activity later service...");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}