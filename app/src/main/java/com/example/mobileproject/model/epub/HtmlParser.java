package com.example.mobileproject.model.epub;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlParser {
    private String[] htmlTags = {"html","base","head","style","title","address","article","footer","header","h1","h2","h3","h4","h5","h6","hgroup","nav","section","dd","div","dl","dt","figcaption","figure","hr","li","main","ol","p","pre","ul","abbr","b","bdi","bdo","br","cite","code","data","dfn","em","i","kbd","mark","q","rp","rt","rtc","ruby","s","samp","small","span","strong","sub","sup","time","u","var","wbr","area","audio","map","track","video","embed","object","param","source","canvas","noscript","script","del","ins","caption","col","colgroup","table","tbody","td","tfoot","th","thead","tr","button","datalist","fieldset","form","input","keygen","label","legend","meter","optgroup","option","output","progress","select","details","dialog","menu","menuitem","summary","content","element","shadow","template","acronym","applet","basefont","big","blink","center","dir","frame","frameset","isindex","listing","noembed","plaintext","spacer","strike","tt","xmp"};

    private final ArrayList<String> defaultPreservedTags = new ArrayList<>(Arrays.asList("html", "body","img", "p", "b", "i", "span", "ul", "li", "a"));

    public HtmlParser() {
    }

    public Document removeUnnecessaryTags(Document html, String[] preservedTags){
        for(String tag : preservedTags){
            if(!defaultPreservedTags.contains(tag)){
                defaultPreservedTags.add(tag);
            }
        }

        for (String tag : htmlTags) {
            if ( !defaultPreservedTags.contains(tag) ){
                html.select(tag).remove();
            }
        }

        return html;
    }

    public Document removeUnnecessaryTags(Document html){
        return removeUnnecessaryTags(html, new String[]{});
    }

    public String CleanChapter(String chapter){
        String cleanedChapter = chapter;

        for(String preservedTag : defaultPreservedTags){
            if(preservedTag.equals("img")){
                continue;
            }

            if(preservedTag.equals("b")){
                cleanedChapter = cleanedChapter.replaceAll("<b (.*?)>", "->2");
                cleanedChapter = cleanedChapter.replaceAll("<b>", "->2");
                cleanedChapter = cleanedChapter.replaceAll("</b>", "2<-");

                continue;
            }

            if (preservedTag.equals("i")){
                cleanedChapter = cleanedChapter.replaceAll("<i (.*?)>", "->3");
                cleanedChapter = cleanedChapter.replaceAll("<i>", "->3");
                cleanedChapter = cleanedChapter.replaceAll("</i>", "3<-");

                continue;
            }

            if (preservedTag.equals("li")){
                cleanedChapter = cleanedChapter.replaceAll("<li(.*?)>", "\\u0009\\u0009\\u0009\\u0009•");
                cleanedChapter = cleanedChapter.replaceAll("</li>", "\n\n");

                continue;
            }

            cleanedChapter = cleanedChapter.replaceAll("<" + preservedTag + "(.*?)>", "");
            cleanedChapter = cleanedChapter.replaceAll("</" + preservedTag + ">", "\n\n");
        }

        return cleanedChapter
                .replaceAll("<!--(.*?)-->", "")
                .replaceAll("  ", "")
                .replaceAll("\n  ", "\n")
                .replaceAll("\n ", "\n")
                .replaceAll("  \n", "\n")
                .replaceAll(" \n", "\n")
                .replaceAll("\\n\\s+(.*?)", "\n\n")
                .trim();
    }
}
