package com.example.mobileproject.model;

import com.example.mobileproject.ChaptersAdapter;

import java.io.Serializable;
import java.util.Objects;

public class ChapterIndex implements Serializable, Comparable<ChapterIndex> {
    private int id = -1;

    private String chapterName;
    private String chapterLink;

    private int source_id;

    private ChapterStatus status = ChapterStatus.EMPTY;
    private String readed = "no";

    private int lastPageReaded = 0;

    public boolean selected = false;
    public int position = -1;

    public ChapterIndex() {
    }

    public ChapterIndex(String chapterName, String chapterLink, int source_id) {
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
        this.source_id = source_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChapterIndex that = (ChapterIndex) o;
        return chapterName.equals(that.chapterName) && chapterLink.equals(that.chapterLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chapterName, chapterLink, source_id);
    }

    @Override
    public int compareTo(ChapterIndex chapterIndex) {
        return Integer.compare(this.id, chapterIndex.getId());
    }

    public void updateSelf(ChapterIndex c){
        this.id = c.getId();
        this.readed = c.getReaded();
        this.status = c.getStatus();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ChapterStatus getStatus() {
        return status;
    }

    public void setStatus(ChapterStatus status) {
        this.status = status;
    }

    public String getReaded() {
        return readed;
    }

    public void setReaded(String readed) {
        this.readed = readed;
    }

    public int getSourceId() {
        return source_id;
    }

    public void setSourceId(int source_id) {
        this.source_id = source_id;
    }

    public int getLastPageReaded() {
        return lastPageReaded;
    }

    public void setLastPageReaded(int lastPageReaded) {
        this.lastPageReaded = lastPageReaded;
    }
}
