package com.example.mobileproject.model.parser;

import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

public interface ParserInterface {
    public NovelDetails getNovelDetails(String novelLink);
    public ArrayList<ChapterIndex> getAllChaptersIndex(String novelLink);
    public ArrayList<NovelDetailsMinimum> getHotNovels();
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue);
    public ChapterContent getChapterContent(String chapterUrl);
    public ArrayList<ChapterIndex> checkNewChapters(String novelLink, int page);
    public int getLastPageSearched();
    public int getSourceType();

    public ParserInterface getParserInstance();
}
