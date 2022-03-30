package com.example.mobileproject.services;

import static com.example.mobileproject.App.CHANNEL_ID;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.CheckUpdatesItem;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
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

    private ResultReceiver libraryFragmentReceiver;
    private ResultReceiver novelDetailsActivityReceiver;
    private ResultReceiver readerActivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = NotificationManagerCompat.from(this);

        PowerManager powerManager =  (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExampleApp:WakeLock");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        DBController db = new DBController(getApplicationContext());
        novelList = db.selectOnGoingNovels();

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Atualizando a Biblioteca")
                .setSmallIcon(R.drawable.ic_baseline_android)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false);

        new Thread(new CheckUpdateWorker()).start();

        startForeground(2, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ResultReceiver resultReceiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        if(resultReceiver != null){
            libraryFragmentReceiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        }
        ResultReceiver resultReceiver2 = (ResultReceiver) intent.getParcelableExtra("receiver2");
        if(resultReceiver2 != null){
            novelDetailsActivityReceiver = (ResultReceiver) intent.getParcelableExtra("receiver2");
        }
        ResultReceiver resultReceiver3 = (ResultReceiver) intent.getParcelableExtra("receiver3");
        if(resultReceiver3 != null){
            readerActivityReceiver = (ResultReceiver) intent.getParcelableExtra("receiver3");
        }

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
                SystemClock.sleep(2000);

                CheckUpdatesItem newItem = CheckUpdate(novelList.get(0));
                if (!newItem.isEmpty())
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

                notificationManager.notify(2, notification.build());

                PutChaptersToDownload();

                Intent serviceIntent = new Intent(getApplicationContext(), DownloaderService.class);
                getApplicationContext().startService(serviceIntent);

                if(libraryFragmentReceiver != null){
                    libraryFragmentReceiver.send(0, new Bundle());
                }

                if(novelDetailsActivityReceiver != null){
                    Bundle resultData = new Bundle();
                    resultData.putSerializable("updatedNovelsList" , checkUpdatesItems);

                    novelDetailsActivityReceiver.send(0, resultData);
                }

                if(readerActivityReceiver != null){
                    Bundle resultData = new Bundle();
                    resultData.putSerializable("updatedNovelsList" , checkUpdatesItems);

                    readerActivityReceiver.send(0, resultData);
                }
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
            ParserInterface parser = ParserFactory.getParserInstance(novelDetails.getSource(), getApplicationContext());

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
                for (int j = 0; j < currentItem.getNewChapters().size(); j++) {
                    ChapterIndex currentChapter = currentItem.getNewChapters().get(j);
                    db.putChapterOnDownload(
                            currentItem.getNovelDetails().getNovelName(),
                            currentItem.getNovelDetails().getSource(),
                            currentChapter.getChapterLink()
                    );
                }
            }
        }
    }

}
