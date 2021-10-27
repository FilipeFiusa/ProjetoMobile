package com.example.mobileproject.model;

import static com.example.mobileproject.App.CHANNEL_ID;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;

import java.util.ArrayList;

public class CheckUpdateService extends Service {
    public static final String TAG = "DownloaderService";

    private boolean isCheckingDB = false;
    public static ArrayList<DownloaderClass> downloader = new ArrayList<>();
    public static ArrayList<NovelDetails> novelList = new ArrayList<>();

    private NotificationManagerCompat notificationManager;
    private PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder notification;

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Startou");

        notificationManager = NotificationManagerCompat.from(this);

        PowerManager powerManager =  (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExampleApp:WakeLock");
        wakeLock.acquire();

        DBController db = new DBController(getApplicationContext());
        novelList = db.selectAllNovels();

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading")
                .setContentText("Downloading")
                .setSmallIcon(R.drawable.ic_baseline_android)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false);

        //CheckDownloadQueueOnDB();

        new Thread(new CheckUpdateWorker()).start();

        startForeground(2, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        notification
                .setContentText("Download Finished")
                .setOngoing(false)
                .setProgress(0, 0, false);
        notificationManager.notify(2, notification.build());

        wakeLock.release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CheckUpdateWorker implements Runnable {
        @Override
        public void run() {
            int totalSize = novelList.size();
            int progress = 0;

            while (!novelList.isEmpty()){
                SystemClock.sleep(3000);

                notification
                        .setContentTitle("Atualizando Bibiloteca - (" + (progress + 1) + "/" + totalSize + ")")
                        .setContentText("")
                        .setProgress(totalSize, progress+1, false);
                notificationManager.notify(2, notification.build());
                SystemClock.sleep(2000);

                progress++;
                novelList.remove(0);
            }

            notification
                    .setContentText("Finalizado")
                    .setOngoing(false)
                    .setProgress(0, 0, false);
            notificationManager.notify(2, notification.build());

            stopForeground(false);
            notificationManager.cancel(2);
            stopSelf();
        }
    }
}
