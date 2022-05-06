package com.example.mobileproject.model;

import java.io.Serializable;
import java.util.Objects;

public class Chapter implements Serializable {
    private boolean exist = true;
    private int invalidType = 1; // 1- No more chapters, 2- No internet

    private ChapterIndex chapterIndex = null;
    private ChapterContent chapterContent = null;

    private int totalPages = 0;

    public Chapter() {
        exist = false;
    }

    public Chapter(int invalidType) {
        this.invalidType = invalidType;
        exist = false;
    }

    public Chapter(ChapterIndex chapterIndex, ChapterContent chapterContent) {
        this.chapterIndex = chapterIndex;
        this.chapterContent = chapterContent;
    }

    public ChapterIndex getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(ChapterIndex chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public ChapterContent getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(ChapterContent chapterContent) {
        this.chapterContent = chapterContent;
    }

    public boolean isChapterEmpty(){
        if (chapterContent == null) return false;
        return chapterContent.getChapterContent().length() == 0;
    }

    public boolean exist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public int getInvalidType() {
        return invalidType;
    }

    public void setInvalidType(int invalidType) {
        this.invalidType = invalidType;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void resetTotalPages(){
        this.totalPages = 0;
    }

    public void addPage() {
        this.totalPages += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chapter chapter = (Chapter) o;
        if(!exist) return false;
        if(!chapter.exist) return false;
        return chapterIndex.getChapterLink().equals(chapter.chapterIndex.getChapterLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(chapterIndex);
    }
}
