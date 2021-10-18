package com.example.mobileproject.model;

import android.widget.ImageButton;

public class ChapterAdapterReference {
    private ImageButton imageButton;
    private int chapter_id;

    public ChapterAdapterReference(ImageButton imageButton, int chapter_id) {
        this.imageButton = imageButton;
        this.chapter_id = chapter_id;
    }

    public ImageButton getImageButton() {
        return imageButton;
    }

    public int getChapter_id() {
        return chapter_id;
    }
}
