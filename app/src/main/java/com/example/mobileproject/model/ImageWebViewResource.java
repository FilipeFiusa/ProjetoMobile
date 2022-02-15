package com.example.mobileproject.model;

import android.graphics.Bitmap;

public class ImageWebViewResource {
    public int position;

    private Bitmap imageResource;

    private String imageResourceLink;
    private String alt;

    public ImageWebViewResource(String imageResourceLink, String alt) {
        this.imageResourceLink = imageResourceLink;
        this.alt = alt;
    }

    public Bitmap getImageResource() {
        return imageResource;
    }

    public void setImageResource(Bitmap imageResource) {
        this.imageResource = imageResource;
    }

    public String getImageResourceLink() {
        return imageResourceLink;
    }

    public void setImageResourceLink(String imageResourceLink) {
        this.imageResourceLink = imageResourceLink;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

}
