package com.example.mobileproject.services.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.mobileproject.ChaptersAdapter;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.ReaderChaptersAdapter;

public class DownloadRAReceiver extends ResultReceiver {
    private final ReaderActivity readerActivity;

    public DownloadRAReceiver(Handler handler, ReaderActivity readerActivity) {
        super(handler);
        this.readerActivity = readerActivity;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        int chapter_id = resultData.getInt("chapter_id");
        boolean hadError = resultData.getBoolean("error");

        if(readerActivity != null){
            readerActivity.ChapterDownloaded(chapter_id, hadError);
        }
    }
}
