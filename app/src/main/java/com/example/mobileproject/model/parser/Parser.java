package com.example.mobileproject.model.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.Languages;
import com.example.mobileproject.ui.navigate.SourceItemAdapter;
import com.example.mobileproject.util.HtmlCleaner;

import org.intellij.lang.annotations.Language;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.util.ArrayList;

public abstract class Parser implements ParserInterface {
    protected String URL_BASE = "";
    protected String SourceName = "";
    protected Drawable Icon;
    protected Languages language;

    protected int sourceType = 1; // 1- Normal update, 2- Paginated update
    protected int lastPageSearched = 1;

    protected Context ctx;

    protected int parserType = 3; // 3- Full implementation / 2- not full implementation / 1- WebView implementation

    public boolean isPinned;
    public boolean isActive;

    public SourceItemAdapter adapterReference = null;
    public int position;

    public Parser(Context ctx) {
        this.ctx = ctx;
    }

    public String getSourceName() {
        return SourceName;
    }

    public Drawable getIcon() {
        return Icon;
    }

    public String getURL_BASE(){ return URL_BASE; }

    @Override
    public ArrayList<ChapterIndex> checkNewChapters(String novelLink, int page) {
        if(sourceType == 1){
            return getAllChaptersIndex(novelLink);
        }else if(sourceType == 2){
            return getPaginatedChapters(novelLink, page);
        }

        return new ArrayList<>();
    }

    protected ArrayList<ChapterIndex> getPaginatedChapters(String novelLink, int page){
        return new ArrayList<>();
    }

    protected void removeComments(Node node) {
        for (int i = 0; i < node.childNodeSize();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }

    //This function gets a jsoup document to clean unwanted html tags
    //like <script>, comments, ads, etc.
    //override and use super.CleanDocument if needed.
    protected void cleanDocument(Document document){
        removeComments(document);
        document.select(".google-auto-placed").remove();
        document.select(".ap_container").remove();
        document.select(".ads").remove();
        document.select(".ads-box").remove();
        document.select(".adsbox").remove();
        document.select(".ads-holder").remove();
        document.select(".ads-middle").remove();
        document.select("ins").remove();
        document.select("script").remove();
        document.select("style").remove();
        document.select("iframe").remove();
    }

    protected String cleanDescription(String content){
        return HtmlCleaner.CleanText(cleanHTMLEntities(content));
    }

    protected String cleanChapter(String _content){
        String content = _content;

        content = cleanHTMLEntities(content);
        content = removeUselessElements(content);

        return HtmlCleaner.CleanText(content.replaceAll("\r", "\n"));
    }

    protected String cleanHTMLEntities(String Content){
        return Content
                .replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "'")
                .replaceAll("&nbsp;", " ");
    }

    protected String removeUselessElements(String content){
        Document html = Jsoup.parse(content);

        html.select(".google-auto-placed").remove();
        html.select(".ap_container").remove();
        html.select(".ads").remove();
        html.select(".ads-box").remove();
        html.select(".adsbox").remove();
        html.select(".ads-holder").remove();
        html.select(".ads-middle").remove();
        html.select("ins").remove();
        html.select("script").remove();
        html.select("style").remove();
        html.select("iframe").remove();

        return html.html();
    }

    public Languages getLanguage() {
        return language;
    }

    @Override
    public int getLastPageSearched() {
        return lastPageSearched;
    }

    @Override
    public int getSourceType() {
        return sourceType;
    }
}
