package com.example.mobileproject.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class NovelDetailsMinimum {
    private int id;

    private Bitmap novelImage;
    private String novelImageSrc;
    private java.lang.String novelName;
    private java.lang.String novelLink;

    public NovelDetailsMinimum(Bitmap novelImage, java.lang.String novelName, java.lang.String novelLink) {
        this.novelImage = novelImage;
        this.novelName = novelName;
        this.novelLink = novelLink;
    }

    public NovelDetailsMinimum(int id, String novelImage, java.lang.String novelName, java.lang.String novelLink) {
        this.id = id;
        this.novelImageSrc = novelImage;
        this.novelName = novelName;
        this.novelLink = novelLink;
    }

    public Bitmap getNovelImage() {
        return novelImage;
    }

    public void setNovelImage(Bitmap novelImage) {
        this.novelImage = novelImage;
    }

    public java.lang.String getNovelName() {
        return novelName;
    }

    public void setNovelName(java.lang.String novelName) {
        this.novelName = novelName;
    }

    public java.lang.String getNovelLink() {
        return novelLink;
    }

    public void setNovelLink(java.lang.String novelLink) {
        this.novelLink = novelLink;
    }

    public String getNovelImageSrc() {
        return novelImageSrc;
    }

    public void setNovelImageSrc(String novelImageSrc) {
        this.novelImageSrc = novelImageSrc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
