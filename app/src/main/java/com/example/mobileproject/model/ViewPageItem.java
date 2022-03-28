package com.example.mobileproject.model;

import java.util.Objects;

public class ViewPageItem {
    private int type; // 1- Page with title, 2- Page without title, 3- No previous chapter, 4- No next chapter

    private Chapter chapter;

    private String title;
    private String chapterContent;
    private int currentPage;

    private boolean isLastPage = false;

    public ViewPageItem(String title, String chapterContent, Chapter chapter) {
        this.title = title;
        this.chapterContent = chapterContent;
        this.chapter = chapter;
        this.currentPage = 1;

        this.type = 1;
    }

    public ViewPageItem(String chapterContent, Chapter chapter, int currentPage) {
        this.chapterContent = chapterContent;
        this.chapter = chapter;
        this.currentPage = currentPage;

        this.type = 2;
    }

    public ViewPageItem(Chapter chapter) {
        this.chapter = chapter;
        this.type = 3;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewPageItem that = (ViewPageItem) o;
        if(type == 3 && type == that.type) return true;
        return chapter.equals(that.chapter);
    }
}
