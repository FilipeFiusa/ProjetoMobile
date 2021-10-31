package com.example.mobileproject.model;

import static com.example.mobileproject.App.CHANNEL_ID;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.util.ArrayList;
import java.util.List;

public class CheckUpdateService extends Service {
    public static final String TAG = "DownloaderService";

    private ArrayList<CheckUpdatesItem> checkUpdatesItems = new ArrayList<>();

    private ArrayList<NovelDetails> novelList = new ArrayList<>();

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
                .setContentTitle("Atualizando a Biblioteca")
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

            notification
                    .setContentTitle("Atualizando Bibiloteca - (" + (0) + "/" + totalSize + ")")
                    .setContentText("")
                    .setProgress(totalSize, 0, false);
            notificationManager.notify(2, notification.build());

            while (!novelList.isEmpty()){
                SystemClock.sleep(3000);

                CheckUpdatesItem newItem = CheckUpdate(novelList.get(0));
                checkUpdatesItems.add(newItem);

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

            PutChaptersToDownload();

            if(!checkUpdatesItems.isEmpty()){

                StringBuilder message = new StringBuilder();
                for (int i = 0; i < checkUpdatesItems.size(); i++) {
                    String m = checkUpdatesItems.get(i).toString();
                    if(i==0){
                        message.append("  ");
                    }else{
                        message.append("\n  ");
                    }
                    message.append(m);
                }

                notification
                        .setContentTitle("Novos capitulos: ")
                        .setOngoing(false)
                        .setProgress(0, 0, false)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

                SystemClock.sleep(3000);

                Intent serviceIntent = new Intent(getApplicationContext(), DownloaderService.class);
                getApplicationContext().startService(serviceIntent);

            }else{
                notification
                        .setContentTitle("Finalizado")
                        .setContentText("Nenhum Capitulo Encontrado")
                        .setOngoing(false)
                        .setProgress(0, 0, false);
            }


            stopForeground(false);
            notificationManager.cancel(2);
            stopSelf();
        }

        public CheckUpdatesItem CheckUpdate(NovelDetails novelDetails) {
            ArrayList<ChapterIndex> newChapters = new ArrayList<>();

            DBController db = new DBController(getApplicationContext());
            Parser parser = new NovelFullParser();

            List<ChapterIndex> chapterList = db.getChaptersFromANovel(novelDetails.getNovelName(), novelDetails.getSource());
            ArrayList<ChapterIndex> tempList = parser.getAllChaptersIndex(novelDetails.getNovelLink());

            for (int i = 0; i < tempList.size(); i++) {
                boolean updated = false;
                ChapterIndex currentItem = tempList.get(i);

                for (ChapterIndex o : chapterList) {
                    ChapterIndex aux = o;
                    if (currentItem.equals(aux)) {
                        currentItem.updateSelf(aux);
                        updated = true;
                        break;
                    }
                }

                if (!updated) {
                    System.out.println(currentItem.getChapterName());
                    currentItem.setId(-1);
                    newChapters.add(currentItem);
                }
            }

            db.updateChapters(novelDetails.getNovelName(), novelDetails.getSource(), tempList);

            return new CheckUpdatesItem(novelDetails, newChapters);
        }

        public void PutChaptersToDownload(){
            DBController db = new DBController(getApplicationContext());

            for (int i = 0; i < checkUpdatesItems.size(); i++) {
                CheckUpdatesItem currentItem = checkUpdatesItems.get(i);
                for (int j = 0; j < currentItem.newChapters.size(); j++) {
                    ChapterIndex currentChapter = currentItem.newChapters.get(j);
                    db.putChapterOnDownload(
                            currentItem.novelDetails.getNovelName(),
                            currentItem.novelDetails.getSource(),
                            currentChapter.getChapterLink()
                    );
                }
            }
        }
    }

    private static class CheckUpdatesItem{
        private final NovelDetails novelDetails;
        private final ArrayList<ChapterIndex> newChapters;

        public CheckUpdatesItem(NovelDetails novelDetails, ArrayList<ChapterIndex> newChapters) {
            this.novelDetails = novelDetails;
            this.newChapters = newChapters;
        }

        public NovelDetails getNovelDetails() {
            return novelDetails;
        }

        public ArrayList<ChapterIndex> getNewChapters() {
            return newChapters;
        }

        @Override
        public String toString() {
            StringBuilder returning = new StringBuilder();

            returning.append(getInitials(novelDetails.getNovelName()));

            returning.append(": ");
            returning.append(newChapters.size());

            return returning.toString();
        }

        public String getInitials(String fullname){
            StringBuilder initials = new StringBuilder();
            for (String s : fullname.split(" ")) {
                initials.append(s.charAt(0));
            }
            return initials.toString();
        }
    }

}
