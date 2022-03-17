package com.example.mobileproject.model;

import java.io.Serializable;
import java.util.ArrayList;

public class NovelReaderController implements Serializable {
    private int position;
    private ArrayList<ChapterIndex> chapterIndices;

    private Chapter previousChapter = new Chapter();
    private Chapter currentChapter = new Chapter();
    private Chapter nextChapter = new Chapter();

    public NovelReaderController(ArrayList<ChapterIndex> chapterIndices) {
        this.chapterIndices = chapterIndices;
    }

    public ChapterIndex setStartedChapter(String chapterLink){
        for (int i = 0; i < chapterIndices.size(); i++) {
            ChapterIndex c = chapterIndices.get(i);

            if(c.getChapterLink().equals(chapterLink)){
                position = i;

                currentChapter = new Chapter(c, null);

                if(position - 1 >= 0){
                    previousChapter = new Chapter(chapterIndices.get(position - 1), null);
                }

                if(position + 1 < chapterIndices.size()){
                    nextChapter = new Chapter(chapterIndices.get(position + 1), null);
                    System.out.println(nextChapter.getChapterIndex().getChapterName());
                }

                return c;
            }
        }
        return null;
    }

    /*public ChapterIndex getCurrentChapter(){
        if(chapterIndices.size() == 0) return new ChapterIndex();
        return chapterIndices.get(position);
    }*/

    public Chapter getCurrentChapter(){
        if(chapterIndices.size() == 0) return new Chapter(null, null);
        return currentChapter;
    }

    public Chapter getPreviousChapter(){
        if(previousChapter.getChapterIndex() != null){
            Chapter oldCurrentChapter = new Chapter(currentChapter.getChapterIndex(), currentChapter.getChapterContent());

            currentChapter = previousChapter;
            nextChapter = oldCurrentChapter;

            if (position > 1){
                previousChapter = new Chapter(chapterIndices.get(position - 2), null);
                position = position - 1;
            }else {
                previousChapter = new Chapter();
            }


            return currentChapter;
        }

        return null;
    }

    public Chapter getNextChapter(){
        if(nextChapter.getChapterIndex() != null){
            Chapter oldCurrentChapter = new Chapter(currentChapter.getChapterIndex(), currentChapter.getChapterContent());

            currentChapter = nextChapter;
            previousChapter = oldCurrentChapter;

            if (position + 1 < chapterIndices.size() - 1){
                nextChapter = new Chapter(chapterIndices.get(position + 2), null);
                position = position + 1;
            }else {
                nextChapter = new Chapter();
            }

            return currentChapter;
        }

        return null;
    }

/*    public ChapterIndex goToChapter(int newPosition){
        position = newPosition;
        return chapterIndices.get(newPosition);
    }    */

    public Chapter goToChapter(int newPosition){
        position = newPosition;

        currentChapter = new Chapter(chapterIndices.get(position), null);

        if(position - 1 > 0){
            previousChapter = new Chapter(chapterIndices.get(position - 1), null);
        }else {
            previousChapter = new Chapter();
        }

        if(position + 1 < chapterIndices.size() - 1){
            nextChapter = new Chapter(chapterIndices.get(position + 1), null);
        }else {
            nextChapter = new Chapter();
        }


        return currentChapter;
    }

    public void updateChaptersList(ChapterIndex currentChapter, ArrayList<ChapterIndex> chapters){
        this.chapterIndices = chapters;

        setStartedChapter(currentChapter.getChapterLink());
    }

    public void updateCurrentPreviousChapter(ChapterContent chapterContent) {
        this.previousChapter.setChapterContent(chapterContent);
    }

    public Chapter getCurrentPreviousChapter() {
        return previousChapter;
    }

    public void updateCurrentNextChapter(ChapterContent chapterContent) {
        this.nextChapter.setChapterContent(chapterContent);
    }

    public Chapter getCurrentNextChapter() {
        return nextChapter;
    }

    public void updateCurrentChapter(ChapterContent chapterContent) {
        this.currentChapter.setChapterContent(chapterContent);
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
