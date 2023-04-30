package com.example.mobileproject.services;

import static com.example.mobileproject.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.util.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

public class CheckNovelUpdatesService extends JobService {
    public static final String TAG = "DownloaderService";

    private ArrayList<CheckUpdatesItem> checkUpdatesItems = new ArrayList<>();
    private ArrayList<NovelDetails> novelList = new ArrayList<>();

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notification;

    final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};

    int SUMMARY_ID = 0;
    String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        notificationManager = NotificationManagerCompat.from(this);

        DBController db = new DBController(getApplicationContext());
        novelList = db.selectOnGoingNovels();

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Atualizando a Biblioteca")
                .setSmallIcon(R.drawable.ic_baseline_android)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false);

        startForeground(2, notification.build());

        new Thread(new CheckUpdateWorker(jobParameters)).start();

        /*

        Intent serviceIntent = new Intent(this, CheckUpdateService.class);
        startService(serviceIntent);


        jobFinished(jobParameters, false);*/
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private class CheckIfServiceIsRunning implements Runnable{
        private final JobParameters jobParameters;

        public CheckIfServiceIsRunning(JobParameters jobParameters) {
            this.jobParameters = jobParameters;
        }

        @Override
        public void run() {
            while (ServiceHelper.isMyServiceRunning(getApplicationContext(), CheckUpdateService.class)){
                SystemClock.sleep(1000);
            }

            jobFinished(jobParameters, true);
        }
    }

    private class CheckUpdateWorker implements Runnable {

        private JobParameters params;

        public CheckUpdateWorker(final JobParameters params) {
            this.params = params;
        }

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
                if(!newItem.isEmpty())
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
                showNewChaptersNotification();

                PutChaptersToDownload();

                Intent serviceIntent = new Intent(getApplicationContext(), DownloaderService.class);
                getApplicationContext().startService(serviceIntent);
            }else{
                notification
                        .setContentTitle("Finalizado")
                        .setContentText("Nenhum Capitulo Encontrado")
                        .setOngoing(false)
                        .setProgress(0, 0, false);
            }

            notificationManager.cancel(2);
            stopForeground(false);
            jobFinished(params, false);
        }

        public CheckUpdatesItem CheckUpdate(NovelDetails novelDetails) {
            ArrayList<ChapterIndex> newChapters = new ArrayList<>();

            DBController db = new DBController(getApplicationContext());
            ParserInterface parser = ParserFactory.getParserInstance(novelDetails.getSource(), getApplicationContext());

            System.out.println(novelDetails.getSource());
            System.out.println(novelDetails.getNovelLink());
            System.out.println(novelDetails.getNovelName());

            ArrayList<ChapterIndex> chapterList = db.getChaptersFromANovel(novelDetails.getNovelName(), novelDetails.getSource());
            ArrayList<ChapterIndex> tempList = new ArrayList<>();
            try{
                tempList = parser.checkNewChapters(novelDetails.getNovelLink(), novelDetails.getLastPageSearched());
            }catch (Exception e){
                System.out.println(e.getMessage());
                return new CheckUpdatesItem(novelDetails, newChapters);
            }

            if(tempList == null || tempList.isEmpty()){
                return new CheckUpdatesItem(novelDetails, newChapters);
            }

            if(parser.getSourceType() == 1){
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
            }else if(parser.getSourceType() == 2){
                int count = 0;
                while (true){
                    boolean alreadyExists = false;
                    ChapterIndex currentItem = tempList.get(count);

                    for (int j = 0; j < chapterList.size(); j++) {
                        ChapterIndex aux = chapterList.get(j);
                        if (currentItem.equals(aux)) {
                            tempList.remove(currentItem);
                            alreadyExists = true;
                            break;
                        }
                    }

                    if(alreadyExists){
                        count = 0;

                        if(tempList.size() == 0){
                            break;
                        }
                    }else{
                        count++;

                        if(tempList.size() == count){
                            break;
                        }
                    }
                }

                if (tempList.size() > 0){
                    newChapters.addAll(tempList);
                    chapterList.addAll(tempList);

                    for (int i = 0; i < chapterList.size(); i++) {
                        ChapterIndex currentItem = chapterList.get(i);
                        currentItem.setSourceId(i);
                    }

                    novelDetails.setLastPageSearched(parser.getLastPageSearched());
                    db.updateChapters(novelDetails.getNovelName(), novelDetails.getSource(), chapterList, parser.getLastPageSearched());
                }
            }

            return new CheckUpdatesItem(novelDetails, newChapters);
        }

        private void showNewChaptersNotification(){
            String GROUP_NAME = "Novels_Group";
            int newChaptersCount = 0;

            NotificationCompat.InboxStyle summaryNotificationStyle = new NotificationCompat.InboxStyle();

            for (int i = 0; i < checkUpdatesItems.size(); i++) {
                NovelDetails currentNovel = checkUpdatesItems.get(i).novelDetails;

                // Create an Intent for the activity you want to start
                Intent resultIntent = new Intent(getApplicationContext(), NovelDetailsActivity.class);
                resultIntent.putExtra("NovelDetails_name", currentNovel.getNovelName());
                resultIntent.putExtra("NovelDetails_source", currentNovel.getSource());
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                // Get the PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(i, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_android)
                        .setContentTitle(currentNovel.getNovelName())
                        .setContentText(checkUpdatesItems.get(i).allChaptersToString())
                        .setContentIntent(resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(checkUpdatesItems.get(i).allChaptersToString()))
                        .setLargeIcon(currentNovel.getNovelImage())
                        .setVibrate(DEFAULT_VIBRATE_PATTERN)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                        .setGroup(GROUP_NAME)
                        .build();

                notificationManager.notify(10 + i, notification);

                summaryNotificationStyle.addLine(currentNovel.getNovelName() + "  -  " + checkUpdatesItems.get(i).allChaptersToString());

                newChaptersCount += checkUpdatesItems.get(i).newChapters.size();
            }

            summaryNotificationStyle.setBigContentTitle(newChaptersCount + " novos capitulos");

            Notification summaryNotification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setStyle(summaryNotificationStyle)
                    .setSmallIcon(R.drawable.ic_baseline_android)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setGroup(GROUP_NAME)
                    .setVibrate(DEFAULT_VIBRATE_PATTERN)
                    .setGroupSummary(true)
                    .build();

            notificationManager.notify(9, summaryNotification);
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

        @NonNull
        @Override
        public String toString() {

            return getInitials(novelDetails.getNovelName()) +
                    ": " +
                    newChapters.size();
        }

        public String allChaptersToString(){
            StringBuilder allChaptersToString = new StringBuilder();

            for (int i = 0; i < newChapters.size(); i++) {
                allChaptersToString.append(newChapters.get(i).getChapterName());

                if(i+1 != newChapters.size()){
                    allChaptersToString.append(", ");
                }
            }

            return allChaptersToString.toString();
        }

        public String getInitials(String fullname){
            StringBuilder initials = new StringBuilder();
            for (String s : fullname.split(" ")) {
                initials.append(s.charAt(0));
            }
            return initials.toString();
        }

        public boolean isEmpty(){
            return newChapters.isEmpty();
        }

    }
}
