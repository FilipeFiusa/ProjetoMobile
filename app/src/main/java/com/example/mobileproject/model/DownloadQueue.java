package com.example.mobileproject.model;

import android.os.AsyncTask;

import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.util.ArrayList;

public class DownloadQueue {
    public static ArrayList<ChapterIndex> queue = new ArrayList<>();

    public static DownloadQueue instance;

    public static AsyncTask task;

    public static DownloadQueue getInstance(){
        if(instance == null) {
            instance = new DownloadQueue();
        }
        return instance;
    }

    public DownloadQueue() {
    }

    public static void addOnQueue(ChapterIndex index){
        if(queue.contains(index)){
            return;
        }

        queue.add(index);
        checkQueue();
    }

    public static void checkQueue(){
        if(
                task.getStatus() == AsyncTask.Status.PENDING
                        || task.getStatus() == AsyncTask.Status.FINISHED
                        || queue.size() > 0){

        }
    }

    private class ChapterDownloader extends AsyncTask<ChapterIndex, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(ChapterIndex... chapterIndices) {
            Parser parser = new NovelFullParser();

            ChapterContent c = parser.getChapterContent(chapterIndices[0].getChapterLink());

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

}
