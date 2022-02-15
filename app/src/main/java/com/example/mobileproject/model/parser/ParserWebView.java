package com.example.mobileproject.model.parser;

import android.content.Context;

import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;

import java.util.ArrayList;

public class ParserWebView extends Parser{
    private long id;

    private String novelImageCssSelector;
    private String novelNameCssSelector;
    private String novelAuthorCssSelector;
    private String novelDescriptionCssSelector;
    private String novelStatusCssSelector;

    private String novelNovelTitleCssSelector;
    private String novelNovelChapterContentCssSelector;

    private String novelNextButtonCssSelector;
    private String novelPreviousButtonCssSelector;

    public ParserWebView(Context ctx) {
        super(ctx);

        this.parserType = 1;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        return null;
    }

    @Override
    public ArrayList<ChapterIndex> getAllChaptersIndex(String novelLink) {
        return null;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        return null;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        return null;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return null;
    }
}
