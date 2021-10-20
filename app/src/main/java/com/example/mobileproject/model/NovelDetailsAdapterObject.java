package com.example.mobileproject.model;

public class NovelDetailsAdapterObject {
    private int viewType;

    private NovelDetails novelDetails;

    private ChapterIndex chapterIndex;

    public NovelDetailsAdapterObject(ChapterIndex c){
        this.chapterIndex = c;
        this.viewType = 0;
    }

    public NovelDetailsAdapterObject(NovelDetails n){
        this.novelDetails = n;
        this.viewType = 1;
    }

    public int getViewType() {
        return viewType;
    }

    public NovelDetails getNovelDetails() {
        return novelDetails;
    }

    public void setNovelDetails(NovelDetails novelDetails) {
        this.novelDetails = novelDetails;
    }

    public ChapterIndex getChapterIndex() {
        return chapterIndex;
    }
}
