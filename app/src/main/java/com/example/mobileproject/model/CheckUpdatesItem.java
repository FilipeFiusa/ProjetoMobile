package com.example.mobileproject.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CheckUpdatesItem{
    private final NovelDetails novelDetails;
    private final ArrayList<ChapterIndex> newChapters;

    public CheckUpdatesItem(NovelDetails novelDetails, ArrayList<ChapterIndex> newChapters) {
        this.novelDetails = novelDetails;
        this.newChapters = newChapters;
    }

    public NovelDetails getNovelDetails() {
        return novelDetails;
    }

    public ArrayList<ChapterIndex> getNewChapters() {
        return newChapters;
    }

    @NonNull
    @Override
    public String toString() {

        return getInitials(novelDetails.getNovelName()) +
                ": " +
                newChapters.size();
    }

    public String getInitials(String fullName){
        StringBuilder initials = new StringBuilder();
        for (String s : fullName.split(" ")) {
            initials.append(s.charAt(0));
        }
        return initials.toString();
    }

    public boolean isEmpty(){
        return newChapters.isEmpty();
    }
}
