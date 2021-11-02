package com.example.mobileproject.model.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

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
}
