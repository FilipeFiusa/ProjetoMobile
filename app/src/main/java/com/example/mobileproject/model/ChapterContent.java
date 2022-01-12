package com.example.mobileproject.model;

import java.io.Serializable;

public class ChapterContent implements Serializable {
    private String chapterContent;
    private String chapterName;
    private String chapterLink;
    private String rawChapter;

    public ChapterContent() {
    }

    public ChapterContent(String chapterName, String chapterLink) {
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    public ChapterContent(String chapterContent, String chapterName, String chapterLink, String downloaded, String readed) {
        this.chapterContent = chapterContent;
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    public ChapterContent(String chapterContent, String chapterName, String chapterLink) {
        this.chapterContent = chapterContent;
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    public ChapterContent(String chapterContent, String chapterName, String chapterLink, String rawChapter) {
        this.chapterContent = chapterContent;
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
        this.rawChapter = rawChapter;
    }

    public String getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterTitle) {
        this.chapterName = chapterTitle;
    }

    public String getChapterLink() {
        return chapterLink;
    }

    public void setChapterLink(String chapterLink) {
        this.chapterLink = chapterLink;
    }

    public String getRawChapter() {
        return rawChapter;
    }

    public void setRawChapter(String rawChapter) {
        this.rawChapter = rawChapter;
    }
}
