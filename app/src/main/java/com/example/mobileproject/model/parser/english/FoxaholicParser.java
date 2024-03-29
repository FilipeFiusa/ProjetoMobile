package com.example.mobileproject.model.parser.english;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.content.ContextCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.Languages;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class FoxaholicParser extends Parser {

    public FoxaholicParser(Context ctx) {
        super(ctx);

        urlBase = "https://www.foxaholic.com";

        SourceName = "Foxaholic";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_foxaholic);
        language = Languages.ENGLISH;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(urlBase + "/" + novelLink).userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5481.153 Mobile Safari/537.36").get();
            cleanDocument(document);

            //Get the novel name
            String title = document.select(".post-title h1").first().text();

            // Get novel description
            String description = document.select(".summary__content").first().html();

            // Cleaning Description
            description = cleanDescription(description);

            // Get status

            // Get novel author
            String author;
            try {
                author = document.select(".author-content").first().text();
            }catch (NullPointerException e){
                author = "Unknown";
            }

            //Get the novel image
            Element img = document.select(".summary_image img").first();
            java.lang.String imgSrc = img.absUrl("data-src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource(SourceName);
            novelDetails.setNovelLink(novelLink);
            novelDetails.setStatus(1);

            return novelDetails;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<ChapterIndex> getAllChaptersIndex(String novelLink) {
        ArrayList<ChapterIndex> chapterIndices = new ArrayList<>();
        Document d;

        try {
            d = Jsoup.connect(urlBase + "/" + novelLink).userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5481.153 Mobile Safari/537.36").get();

            Elements allLinks = d.select(".listing-chapters_wrap .wp-manga-chapter a");

            for (Element element : allLinks){
                String chapterName = element.text();


                ChapterIndex c = new ChapterIndex(
                        chapterName,
                        element.attr("href").replace(urlBase, ""),
                        chapterIndices.size());

                chapterIndices.add(c);
            }

            Collections.reverse(chapterIndices);
            for (int i = 0; i < chapterIndices.size(); i++) {
                chapterIndices.get(i).setSourceId(i);
            }

            return chapterIndices;
        }catch (IOException e){
            e.printStackTrace();
        }

        return chapterIndices;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(urlBase + "/novel/")
                    .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5481.153 Mobile Safari/537.36")
                    .get();
            Elements novels = document.select(".c-tabs-item .row .page-item-detail");

            for (Element e : novels){
                Element link = e.select("a").first();
                Element img = link.select("img").first();
                String imgSrc = img.absUrl("data-src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        link.attr("title"),
                        link.attr("href").replace(urlBase, "")));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        String _searchValue = removeSpaces(searchValue);
        String url = "https://www.foxaholic.com/?s=" + _searchValue + "&post_type=wp-manga";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5481.153 Mobile Safari/537.36")
                    .get();
            Elements novels = document.select(".search-wrap .row .tab-thumb a");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("data-src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.attr("title"),
                        e.attr("href").replace(urlBase + "/novel/", "")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = urlBase + chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5481.153 Mobile Safari/537.36").get();

            String title = document.select(".select-view select [selected]").first().text();

            String rawChapter = document.html();

            String chapterContent = document.select(".text-left").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new FoxaholicParser(ctx);
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }
}
