package com.example.mobileproject.model;

public class ReaderMenuItem {
    private boolean isSelected = false;

    private ChapterIndex c;

    public ReaderMenuItem(ChapterIndex c, boolean isSelected) {
        this.c = c;
        this.isSelected = isSelected;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public ChapterIndex getChapterIndex() {
        return c;
    }
}
