package com.example.mobileproject.services.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.model.CheckUpdatesItem;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.ui.library.LibraryFragment;

import java.util.ArrayList;

public class CheckUpdatesNDReceiver extends ResultReceiver {
    private final NovelDetailsActivity novelDetails;

    public CheckUpdatesNDReceiver(Handler handler, NovelDetailsActivity novelDetails) {
        super(handler);
        this.novelDetails = novelDetails;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        ArrayList<CheckUpdatesItem> items = (ArrayList<CheckUpdatesItem>) resultData.getSerializable("updatedNovelsList");

        for (CheckUpdatesItem item : items){
            System.out.println(item.getNovelDetails().getNovelName());

            novelDetails.updateChapterList(item.getNovelDetails().getNovelName(), item.getNovelDetails().getSource());
        }

    }
}