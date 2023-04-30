package com.example.mobileproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.services.CheckNovelUpdatesService;

import java.util.ArrayList;

public class App extends Application {
    public static final String CHANNEL_ID = "Check Updates";
    public static final int OLD_CHECK_UPDATES_SERVICE_ID = 123;
    public static final int CHECK_UPDATES_SERVICE_ID = 1234;

    private ArrayList<NovelDetails> novelsOnLibrary;

    public ArrayList<NovelDetails> getNovelsOnLibrary() {
        return novelsOnLibrary;
    }

    public void setNovelsOnLibrary(ArrayList<NovelDetails> novelsOnLibrary) {
        this.novelsOnLibrary = novelsOnLibrary;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        scheduleJob();
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChanel = new NotificationChannel(
                    CHANNEL_ID,
                    "Check Updates Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChanel);
        }
    }

    private void scheduleJob(){
        if(true) return;

        if(isJobServiceOn(this)){
            return;
        }

        ComponentName componentName = new ComponentName(this, CheckNovelUpdatesService.class);
        JobInfo info = new JobInfo.Builder(CHECK_UPDATES_SERVICE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(3 * 60 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(OLD_CHECK_UPDATES_SERVICE_ID);
        scheduler.schedule(info);
    }

    private boolean isJobServiceOn( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == CHECK_UPDATES_SERVICE_ID ) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }
}
