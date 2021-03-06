package com.example.mobileproject.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NovelDetails implements Serializable {
    private int db_id;

    private int novelType;  // 1- novel, 2- Epub

    private Bitmap novelImage;
    private String novelImageLink;

    private String novelName;
    private String novelDescription;
    private String novelAuthor;

    private String source;
    private String novelLink;

    private int status = 0; // 1- Ongoing / 2- Completed / 3- Epub
    private int readerViewType = 1; // 1- Ongoing / 2- Completed

    private int chapterToReadQuantity = 0;

    private String isFavorite = "no";
    private long lastReadied = -1;

    public boolean selected = false;
    public int position;

    private int chapterQuantity = 0;

    private ArrayList<ChapterIndex> chapterIndexes = null;
    private ArrayList<Chapter> chapterList = null;

    private int lastPageSearched = 1;
    private int finishedLoading = 1;

    private String orderType = "DSC";

    public NovelDetails(){
    }

    public NovelDetails(Bitmap novelImage, String novelName, String novelDescription, String novelAuthor, String source, String novelLink) {
        this.novelImage = novelImage;
        this.novelName = novelName;
        this.novelDescription = novelDescription;
        this.novelAuthor = novelAuthor;
        this.source = source;
        this.novelLink = novelLink;
    }

    public NovelDetails(Bitmap novel_image, String novel_name, String novel_description, String novel_author) {
        this.novelImage = novel_image;
        this.novelName = novel_name.replace("'", "");
        this.novelDescription = novel_description;
        this.novelAuthor = novel_author;
    }

    public NovelDetails(Bitmap novel_image, String novel_name, String novel_description, String novel_author, int id) {
        this.novelImage = novel_image;
        this.novelName = novel_name.replace("'", "");
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
        this.novelName = novelName.replace("'", "");
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

    public void setChapterIndexes(List<NovelDetailsAdapterObject> chapterIndexes) {
        this.chapterIndexes = new ArrayList<>();
        for (int i = 0; i < chapterIndexes.size(); i++) {
            this.chapterIndexes.add(chapterIndexes.get(i).getChapterIndex());
        }
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

    public String getNovelLink() {
        return novelLink;
    }

    public void setNovelLink(String novelLink) {
        this.novelLink = novelLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NovelDetails that = (NovelDetails) o;
        return Objects.equals(novelName, that.novelName) && Objects.equals(novelDescription, that.novelDescription) && Objects.equals(novelAuthor, that.novelAuthor) && Objects.equals(source, that.source) && Objects.equals(novelLink, that.novelLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(novelName, novelDescription, novelAuthor, source, novelLink);
    }

    public int getChapterToReadQuantity() {
        return chapterToReadQuantity;
    }

    public void setChapterToReadQuantity(int chapterToReadQuantity) {
        this.chapterToReadQuantity = chapterToReadQuantity;
    }

    public int getDb_id() {
        return db_id;
    }

    public void setDb_id(int db_id) {
        this.db_id = db_id;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLastReadied() {
        return lastReadied;
    }

    public void setLastReadied(long lastReadied) {
        this.lastReadied = lastReadied;
    }

    public int getReaderViewType() {
        return readerViewType;
    }

    public void setReaderViewType(int readerViewType) {
        this.readerViewType = readerViewType;
    }

    public int getFinishedLoading() {
        return finishedLoading;
    }

    public void setFinishedLoading(int finishedLoading) {
        this.finishedLoading = finishedLoading;
    }

    public int getLastPageSearched() {
        return lastPageSearched;
    }

    public void setLastPageSearched(int lastPageSearched) {
        this.lastPageSearched = lastPageSearched;
    }

    public ArrayList<Chapter> getChapterList() {
        return chapterList;
    }

    public void setChapterList(ArrayList<Chapter> chapterList) {
        this.chapterList = chapterList;
    }

    public int getNovelType() {
        return novelType;
    }

    public void setNovelType(int novelType) {
        this.novelType = novelType;
    }

    public String getNovelImageLink() {
        return novelImageLink;
    }

    public void setNovelImageLink(String novelImageLink) {
        this.novelImageLink = novelImageLink;
    }
}
