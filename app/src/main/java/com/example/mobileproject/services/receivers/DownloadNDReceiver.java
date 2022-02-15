package com.example.mobileproject.services.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.mobileproject.ChaptersAdapter;

public class DownloadNDReceiver extends ResultReceiver {
    private final ChaptersAdapter novelDetailsAdapter;

    public DownloadNDReceiver(Handler handler, ChaptersAdapter adapter) {
        super(handler);
        this.novelDetailsAdapter = adapter;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        int chapter_id = resultData.getInt("chapter_id");

        if(novelDetailsAdapter != null){
            novelDetailsAdapter.ChapterDownloaded(chapter_id);
        }
    }
}
