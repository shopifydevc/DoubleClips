package com.vanvatcorporation.doubleclips.services;

import static com.vanvatcorporation.doubleclips.helper.NotificationHelper.CHANNEL_ID_GENERAL;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.vanvatcorporation.doubleclips.activities.MainActivity;
import com.vanvatcorporation.doubleclips.constants.RunnableConstants;
import com.vanvatcorporation.doubleclips.impl.ServiceImpl;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunnableTaskService extends ServiceImpl {


    public static void startTaskInBackground(Context context, Runnable runnable, String title, String description)
    {
        RunnableConstants.RunnableInfo info = RunnableConstants.assignTaskToRunnable(runnable, title, description);

        Intent intent = new Intent(context, RunnableTaskService.class);
        intent.putExtra("RunnableInfo", info);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();


//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
//        Intent stopIntent = new Intent(this, MainActivity.class);
//        stopIntent.setAction(STOP_SERVICE);
//        PendingIntent stopIntentPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null)
        {
            if(intent.getAction() != null)
            {
                switch (intent.getAction())
                {

                }
            }
            if(intent.getSerializableExtra("RunnableInfo") != null)
            {

                RunnableConstants.RunnableInfo info = (RunnableConstants.RunnableInfo) intent.getSerializableExtra("RunnableInfo");
                Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new Notification.Builder(this, CHANNEL_ID_GENERAL) : new Notification.Builder(this);
                Notification notification = builder
                        .setContentTitle(info.title)
                        .setContentText(info.description + " | TaskID: " + info.id)
                        .setSmallIcon(getApplicationInfo().icon)
                        .setProgress(1, 1, true)
                        .build();
                startForeground(1600, notification);


                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {

                    Runnable runnable = () -> {
                        RunnableConstants.getTaskFromId(info.id).run();

                        RunnableConstants.reportCompleteTask(info.id);
                        stopSelf();
                    };
                    runnable.run();
                });

            }

        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }






}