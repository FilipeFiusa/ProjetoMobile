package com.example.mobileproject.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class NovelDetails implements Serializable {
    private int db_id;

    private Bitmap novelImage;

    private String novelName;
    private String novelDescription;
    private String novelAuthor;

    private String source;

    private String isFavorite;

    private int chapterQuantity = 0;

    private ArrayList<ChapterIndex> chapterIndexes = null;

    public NovelDetails(){
    }

    public NovelDetails(Bitmap novel_image, String novel_name, String novel_description, String novel_author) {
        this.novelImage = novel_image;
        this.novelName = novel_name;
        this.novelDescription = novel_description;
        this.novelAuthor = novel_author;
    }

    public NovelDetails(Bitmap novel_image, String novel_name, String novel_description, String novel_author, int id) {
        this.novelImage = novel_image;
        this.novelName = novel_name;
        this.novelDescription = novel_description;
        this.novelAuthor = novel_author;
        this.db_id = id;
    }

    public String getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Bitmap getNovelImage() {
        return novelImage;
    }

    public void setNovelImage(Bitmap novelImage) {
        this.novelImage = novelImage;
    }

    public String getNovelName() {
        return novelName;
    }

    public void setNovelName(String novelName) {
        this.novelName = novelName;
    }

    public String getNovelDescription() {
        return novelDescription;
    }

    public void setNovelDescription(String novelDescription) {
        this.novelDescription = novelDescription;
    }

    public String getNovelAuthor() {
        return novelAuthor;
    }

    public void setNovelAuthor(String novelAuthor) {
        this.novelAuthor = novelAuthor;
    }

    public ArrayList<ChapterIndex> getChapterIndexes() {
        return chapterIndexes;
    }

    public void setChapterIndexes(ArrayList<ChapterIndex> chapterIndexes) {
        this.chapterIndexes = chapterIndexes;
    }

    public int getChapterQuantity() {
        return chapterQuantity;
    }

    public void setChapterQuantity(int chapterQuantity) {
        this.chapterQuantity = chapterQuantity;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
