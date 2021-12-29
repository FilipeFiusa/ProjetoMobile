package com.example.mobileproject.model.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

public abstract class Parser implements ParserInterface {
    protected String URL_BASE;
    protected String SourceName;
    protected Drawable Icon;

    protected Context ctx;

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
        document.select("script").remove();
        document.select("ins").remove();
        document.select(".google-auto-placed").remove();
        document.select(".ap_container").remove();
        document.select(".ads").remove();
        document.select(".ads-holder").remove();
        document.select(".ads-middle").remove();
    }

    protected String cleanChapter(String content){
        return cleanHTMLEntities(content);
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
}
