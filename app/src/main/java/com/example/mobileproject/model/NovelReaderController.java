package com.example.mobileproject.model;

import java.io.Serializable;
import java.util.ArrayList;

public class NovelReaderController implements Serializable {
    private int position;
    private ArrayList<ChapterIndex> chapterIndices;

    public NovelReaderController(ArrayList<ChapterIndex> chapterIndices) {
        this.chapterIndices = chapterIndices;
    }

    public ChapterIndex setStartedChapter(String chapterLink){
        for (int i = 0; i < chapterIndices.size(); i++) {
            ChapterIndex c = chapterIndices.get(i);

            if(c.getChapterLink().equals(chapterLink)){
                position = i;
                return c;
            }
        }
        return null;
    }

    public ChapterIndex getCurrentChapter(){
        return chapterIndices.get(position);
    }

    public ChapterIndex getPreviousChapter(){
        if(position > 0){
            ChapterIndex previous = chapterIndices.get(position - 1);
            position = position - 1;
            return  previous;
        }
        return null;
    }

    public ChapterIndex getNextChapter(){
        if(position < chapterIndices.size() - 1){
            ChapterIndex next = chapterIndices.get(position + 1);
            position = position + 1;
            return  next;
        }
        return null;
    }

    public ChapterIndex goToChapter(int newPosition){
        position = newPosition;
        return chapterIndices.get(newPosition);
    }

    public ArrayList<ChapterIndex> getChapterIndices(){
        return chapterIndices;
    }

    public int getSize(){
        return chapterIndices.size();
    }

    public int getPosition(){
        return position + 1;
    }
}
