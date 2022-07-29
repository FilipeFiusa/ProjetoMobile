package com.example.mobileproject.util;

import java.util.Arrays;
import java.util.List;

public class HtmlCleaner {
    // All possible html tags
    private static final List<String> htmlTags = Arrays.asList("html", "base", "head", "style", "title", "address", "article", "footer", "header", "h1", "h2", "h3", "h4", "h5", "h6", "hgroup", "nav", "section", "dd", "div", "dl", "dt", "figcaption", "figure", "hr", "li", "main", "ol", "p", "pre", "ul", "abbr", "b", "bdi", "bdo", "br", "cite", "code", "data", "dfn", "em", "i", "kbd", "mark", "q", "rp", "rt", "rtc", "ruby", "s", "samp", "small", "span", "strong", "sub", "sup", "time", "u", "var", "wbr", "area", "audio", "map", "track", "video", "embed", "object", "param", "source", "canvas", "noscript", "script", "del", "ins", "caption", "col", "colgroup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "button", "datalist", "fieldset", "form", "input", "keygen", "label", "legend", "meter", "optgroup", "option", "output", "progress", "select", "details", "dialog", "menu", "menuitem", "summary", "content", "element", "shadow", "template", "acronym", "applet", "basefont", "big", "blink", "center", "dir", "frame", "frameset", "isindex", "listing", "noembed", "plaintext", "spacer", "strike", "tt", "xmp", "a", "img", "body");

    private static final List<String> inLineHtmlTags = Arrays.asList("a", "b", "i", "em", "strong");

    // It will remove all tags contained on 'htmlTags' variable
    // If u need to preserve a tag, just send then as an array
    public static String CleanText(String text, String[] preservedTags){
        for(String tag : htmlTags){
            if(Arrays.asList(preservedTags).contains(tag)) {
                System.out.println("Contem a tag: " + tag);

                continue;
            }

            if(tag.equals("hr")){
                text = text.replaceAll("<hr>", "*******\n\n");
            }

            if(inLineHtmlTags.contains(tag)){
                text = text.replaceAll("<" + tag + "(.*?)>", "");
                text = text.replaceAll("</" + tag + ">", "");

                continue;
            }

            text = text.replaceAll("<" + tag + "(.*?)>", "");
            text = text.replaceAll("</" + tag + ">", "\n");
        }

        // Remove multiple line breaks, if there is more than one
        text = text.replaceAll("\\n\\s+(.*?)", "\n\n");

        // Remove comments, if for some reason there is any
        text = text.replaceAll("<!--(.*?)-->", "");

        return text;
    }

    public static String CleanText(String text){
        return CleanText(text, new String[]{});
    }

}
