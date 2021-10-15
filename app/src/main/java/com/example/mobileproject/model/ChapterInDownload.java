package com.example.mobileproject.model;

import android.graphics.Bitmap;

public class ChapterInDownload {
    private ChapterIndex chapterIndex;
    private String novelName;
    private Bitmap novelImage;

    public ChapterInDownload(String chapterName, Bitmap novelImage, ChapterIndex chapterIndex) {
        this.chapterIndex = chapterIndex;
        this.novelName = chapterName;
        this.novelImage = novelImage;
    }

    public ChapterIndex getChapterIndex() {
        return chapterIndex;
    }

    public String getNovelName() {
        return novelName;
    }

    public Bitmap getNovelImage() {
        return novelImage;
    }
}
