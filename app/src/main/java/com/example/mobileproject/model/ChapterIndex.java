package com.example.mobileproject.model;

import java.io.Serializable;

public class ChapterIndex implements Serializable, Comparable<ChapterIndex> {
    private int id;
    private String chapterName;
    private String chapterLink;

    private String downloaded = "no";
    private String readed = "no";

    public ChapterIndex() {
    }

    public ChapterIndex(String chapterName, String chapterLink) {
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    @Override
    public int compareTo(ChapterIndex chapterIndex) {
        return Integer.compare(this.id, chapterIndex.getId());
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterLink() {
        return chapterLink;
    }

    public void setChapterLink(String chapterLink) {
        this.chapterLink = chapterLink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public String getReaded() {
        return readed;
    }

    public void setReaded(String readed) {
        this.readed = readed;
    }
}
