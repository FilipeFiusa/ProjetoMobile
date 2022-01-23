package com.example.mobileproject.services.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.CheckUpdatesItem;

import java.util.ArrayList;

public class CheckUpdatesRAReceiver extends ResultReceiver {
    private final ReaderActivity readerActivity;

    public CheckUpdatesRAReceiver(Handler handler, ReaderActivity readerActivity) {
        super(handler);
        this.readerActivity = readerActivity;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        ArrayList<CheckUpdatesItem> items = (ArrayList<CheckUpdatesItem>) resultData.getSerializable("updatedNovelsList");

        for (CheckUpdatesItem item : items){
            System.out.println(item.getNovelDetails().getNovelName());

            readerActivity.updateChapterList(item.getNovelDetails().getNovelName(), item.getNovelDetails().getSource());
        }

    }
}