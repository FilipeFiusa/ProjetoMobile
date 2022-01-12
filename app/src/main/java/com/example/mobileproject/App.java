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

import com.example.mobileproject.services.CheckNovelUpdatesService;

public class App extends Application {
    public static final String CHANNEL_ID = "App Downloader";
    public static final int CHECK_UPDATES_SERVICE_ID = 123;

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
                    "App Downloader Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChanel);
        }
    }

    private void scheduleJob(){
        //if(true) return;

        if(isJobServiceOn(this)){
            return;
        }

        ComponentName componentName = new ComponentName(this, CheckNovelUpdatesService.class);
        JobInfo info = new JobInfo.Builder(CHECK_UPDATES_SERVICE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(3 * 60 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
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
