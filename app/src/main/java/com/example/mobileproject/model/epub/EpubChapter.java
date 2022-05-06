package com.example.mobileproject.model.epub;

import com.example.mobileproject.model.Chapter;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;

public class EpubChapter {
    private int id;

    private String chapterName;
    private String chapterLink;

    private String rawChapterContent;
    private String cleanedChapterContent;

    public EpubChapter(String chapterName, String chapterLink){
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
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

    public String getRawChapterContent() {
        return rawChapterContent;
    }

    public void setRawChapterContent(String rawChapterContent) {
        this.rawChapterContent = rawChapterContent;
    }

    public String getChapterContent() {
        return cleanedChapterContent;
    }

    public void setChapterContent(String chapterContent) {
        cleanedChapterContent = chapterContent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Chapter toChapter(int chapterNumber){
        ChapterIndex chapterIndex = new ChapterIndex(chapterName, "epub", chapterNumber);
        chapterIndex.setId(id);
        ChapterContent chapterContent = new ChapterContent(cleanedChapterContent, chapterName, "epub", rawChapterContent);

        return new Chapter(chapterIndex, chapterContent);
    }

}
