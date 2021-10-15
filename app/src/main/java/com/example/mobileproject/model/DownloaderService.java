package com.example.mobileproject.model;

import static com.example.mobileproject.App.CHANNEL_ID;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobileproject.R;


public class DownloaderService extends IntentService {
    public static final String TAG = "DownloaderService";
    private NotificationManagerCompat notificationManager;
    PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder notification;

    /*
    private AtomicBoolean working = new AtomicBoolean(true);
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.i("--", "Iniciou");
            SystemClock.sleep(2000);
            Log.i("--", "2s");

            for (int progress = 0; progress <= 100; progress += 10){
                Log.i("--", "rep");

                notification
                        .setContentText(progress + "/100")
                        .setProgress(100, progress, false);
                notificationManager.notify(2, notification.build());
                SystemClock.sleep(1000);
            }
            Log.i("--", "Finalizou");


            notification
                    .setContentText("Download Finished")
                    .setOngoing(false)
                    .setProgress(0, 0, false);
            notificationManager.notify(2, notification.build());

        }
    };

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = NotificationManagerCompat.from(this);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, flags);

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading")
                .setContentText("Downloading this")
                .setSmallIcon(R.drawable.ic_baseline_android)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setProgress(100, 0, false);

        notificationManager.notify(2, notification.build());

        new Thread(runnable).start();

        return START_STICKY;
    }
    */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public DownloaderService() {
        super("DownloaderService");
        setIntentRedelivery(false);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = NotificationManagerCompat.from(this);

        Log.d(TAG, "onCreate");

        PowerManager powerManager =  (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExampleApp:WakeLock");
        wakeLock.acquire();
        Log.d(TAG, "WakeLock acquired");

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading")
                .setContentText("Downloading")
                .setSmallIcon(R.drawable.ic_baseline_android)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false);

        startForeground(1, notification.build());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");

        String novelName = intent.getStringExtra("NovelName");
        String source =  intent.getStringExtra("Source");


        for (int i = 0; i < 100; i++) {

            notification
                    .setContentTitle(novelName)
                    .setContentText(i + "/" + 100)
                    .setProgress(100, i, false);
            notificationManager.notify(1, notification.build());
            SystemClock.sleep(500);

            Log.d(TAG, novelName + " - " + i);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        notification
                .setContentText("Download Finished")
                .setOngoing(false)
                .setProgress(0, 0, false);
        notificationManager.notify(1, notification.build());

        Log.d(TAG, "onDestroy");

        wakeLock.release();
        Log.d(TAG, "WakeLock released");
    }
}
