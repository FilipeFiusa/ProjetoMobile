package com.example.mobileproject.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import kotlin.jvm.internal.CollectionToArray;

public class DownloaderClass implements Serializable {
    private String novelName;
    private String novelSource;

    private ArrayList<ChapterIndex> chapterToDownload;

    public DownloaderClass(String novelName, String novelSource) {
        this.novelName = novelName;
        this.novelSource = novelSource;
    }

    public DownloaderClass(String novelName, String novelSource, ArrayList<ChapterIndex> chapterToDownload) {
        this.novelName = novelName;
        this.novelSource = novelSource;
        this.chapterToDownload = chapterToDownload;
    }

    public String getNovelName() {
        return novelName;
    }

    public void setNovelName(String novelName) {
        this.novelName = novelName;
    }

    public String getNovelSource() {
        return novelSource;
    }

    public void setNovelSource(String novelSource) {
        this.novelSource = novelSource;
    }

    public ArrayList<ChapterIndex> getChapterToDownload() {
        return chapterToDownload;
    }

    public void setChapterToDownload(ArrayList<ChapterIndex> chapterToDownload) {
        this.chapterToDownload = chapterToDownload;
    }

    public void AddOnQueue(ChapterIndex chapter){
        chapterToDownload.add(chapter);
        Collections.sort(chapterToDownload);
    }
}
