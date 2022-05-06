package com.example.mobileproject.model.epub;

import android.graphics.Bitmap;

import com.example.mobileproject.model.Chapter;
import com.example.mobileproject.model.NovelDetails;

import java.util.ArrayList;

public class BookDetails {
    private int id;

    private String bookName;
    private String bookAuthor;
    private String bookPublisher;
    private String bookCoverLink;
    private Bitmap bookCover;

    private ArrayList<EpubChapter> chapterList = new ArrayList<>();

    public BookDetails() {
    }

    public BookDetails(String bookName, String bookAuthor, String bookPublisher, String bookCoverLink) {
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
        this.bookPublisher = bookPublisher;
        this.bookCoverLink = bookCoverLink;
    }

    public String getBookName() {
        return this.bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthor() {
        return this.bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookPublisher() {
        return this.bookPublisher;
    }

    public void setBookPublisher(String bookPublisher) {
        this.bookPublisher = bookPublisher;
    }

    public String getBookCoverLink() {
        return this.bookCoverLink;
    }

    public void setBookCoverLink(String bookCoverLink) {
        this.bookCoverLink = bookCoverLink;
    }

    public Bitmap getBookCover() {
        return this.bookCover;
    }

    public void setBookCover(Bitmap bookCover) {
        this.bookCover = bookCover;
    }

    public ArrayList<EpubChapter> getChapterList() {
        return chapterList;
    }

    public void setChapterList(ArrayList<EpubChapter> chapterList) {
        this.chapterList = chapterList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NovelDetails toNovelDetails(){
        NovelDetails novelDetails = new NovelDetails(bookCover, bookName, "", bookAuthor, bookPublisher, "epub");
        novelDetails.setDb_id(id);
        ArrayList<Chapter> chapters = new ArrayList<>();

        for(int i = 0; i < chapterList.size(); i++){
            EpubChapter currentChapter = chapterList.get(i);

            chapters.add(currentChapter.toChapter(i));
        }

        novelDetails.setChapterList(chapters);

        return novelDetails;
    }
}
