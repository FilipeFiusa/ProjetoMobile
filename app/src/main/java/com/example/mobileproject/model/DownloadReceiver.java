package com.example.mobileproject.model;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.ChaptersAdapter;
import com.example.mobileproject.R;

import java.util.ArrayList;

public class DownloadReceiver extends ResultReceiver {

    private ChaptersAdapter mAdapter;
    private ArrayList<ChapterAdapterReference> referenceArrayList = new ArrayList<>();

    public DownloadReceiver(Handler handler, ChaptersAdapter adapter) {
        super(handler);
        mAdapter = adapter;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        int chapter_id = resultData.getInt("chapter_id");

        mAdapter.ChapterDownloaded(chapter_id);
    }
}