package com.example.mobileproject.model;

import static com.example.mobileproject.App.CHANNEL_ID;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.DoNotInline;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.util.ArrayList;
import java.util.HashMap;


public class DownloaderService extends Service {
    public static final String TAG = "DownloaderService";

    private ResultReceiver downloadReceiver;

    private IBinder mBinder = new MyBinder();
    private Handler mHandler;
    private final ArrayList<DownloaderClass> justRemoved = new ArrayList<>();

    private boolean isCheckingDB = false;
    public static ArrayList<DownloaderClass> downloader = new ArrayList<>();

    private NotificationManagerCompat notificationManager;
    private PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder notification;


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            SystemClock.sleep(1000);

            while (!downloader.isEmpty()){
                DownloaderClass currentNovel = downloader.get(0);
                String title = currentNovel.getNovelName().substring(0, 10);
                String chapterName = currentNovel.getChapterToDownload().getChapterName();

                notification
                        .setContentTitle(title + " - " + chapterName)
                        .setContentText("")
                        .setProgress(1, 0, true);
                notificationManager.notify(1, notification.build());
                SystemClock.sleep(2000);

                DownloadChapter(currentNovel.getChapterToDownload());

                justRemoved.add(downloader.remove(0));

                while (isCheckingDB){
                    SystemClock.sleep(1000);
                }
                CheckDownloadQueueOnDB();
            }

            notification
                    .setContentText("Download Finished")
                    .setOngoing(false)
                    .setProgress(0, 0, false);
            notificationManager.notify(1, notification.build());

            stopSelf();
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

    /*
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public DownloaderService() {
        super("DownloaderService");
        setIntentRedelivery(false);
    }*/

    /*
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
    } */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder{
        DownloaderService getService(){
            return DownloaderService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
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

        CheckDownloadQueueOnDB();

        new Thread(runnable).start();

        startForeground(1, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ResultReceiver test = (ResultReceiver) intent.getParcelableExtra("receiver");
        if(test != null){
            downloadReceiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                isCheckingDB = true;

                CheckDownloadQueueOnDB();

                isCheckingDB = false;
            }
        });

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        notification
                .setContentText("Download Finished")
                .setOngoing(false)
                .setProgress(0, 0, false);
        notificationManager.notify(1, notification.build());

        wakeLock.release();
        Log.d(TAG, "WakeLock released");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    private void CheckDownloadQueueOnDB(){
        DBController d = new DBController(getApplicationContext());
        downloader = d.getDownloadingNovels();
    }

    private void DownloadChapter(ChapterIndex chapterIndex){
        Parser p = new NovelFullParser();
        DBController db = new DBController(getApplicationContext());

        ChapterContent chapterContent = p.getChapterContent(chapterIndex.getChapterLink());

        boolean result = db.setChapterContent(chapterIndex.getId(), chapterContent.getChapterContent());

        Bundle resultData = new Bundle();
        resultData.putInt("chapter_id" ,(int) chapterIndex.getId());
        if(downloadReceiver != null){
            downloadReceiver.send(0, resultData);
        }
    }

    public ArrayList<DownloaderClass> getJustRemoved() {
        return justRemoved;
    }
}
