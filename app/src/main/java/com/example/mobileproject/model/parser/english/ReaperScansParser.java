package com.example.mobileproject.model.parser.english;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import androidx.core.content.ContextCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.Languages;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class ReaperScansParser extends Parser {
    public ReaperScansParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://reaperscans.com";
        SourceName = "ReaperScans";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_reaperscans);
        language = Languages.ENGLISH;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        //.novel-title

        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE +  novelLink).userAgent("Mozilla/5.0").get();
            cleanDocument(document);

            //Get the novel name
            String title = document.select(".post-title h1").first().text();

            // Get novel description
            String description = document.select(".summary__content")
                    .first()
                    .html()
                    .replaceAll("<!--(.*?)-->", "")
                    .replaceAll("\\\\n", "\n")
                    .replaceAll("<div(.*?)>", "")
                    .replaceAll("</div>", "")
                    .replaceAll("<a(.*?)>", "")
                    .replaceAll("</a>", "")
                    .replaceAll("<p> </p>", "")
                    .replaceAll("<p></p>", "")
                    .replaceAll("<strong>", "")
                    .replaceAll("</strong>", "")
                    .replaceAll("\" ", "")
                    .replaceAll(" \"", "")
                    .replaceAll("' ", "")
                    .replaceAll(" '", "")
                    .replaceAll("<p>", "\n")
                    .replaceAll("</p>", "\n")
                    .replaceAll("<br>", "\n")
                    .replaceAll("</br>", "\n")
                    .replaceAll("<hr>", "")
                    .trim();

            // Cleaning Description
            description = cleanHTMLEntities(description);

            // Get status

            // Get novel author
            String author = document.select(".author-content").first().text();

            //Get the novel image
            Element img = document.select(".summary_image img").first();
            String imgSrc = img.absUrl("data-src");
            InputStream input = new URL(imgSrc).openStream();
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

        SystemClock.sleep(3000);

        try {
            d = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();

            d.select(".premium-block").remove();
            Elements allLinks = d.select(".version-chap li");


            System.out.println(allLinks.html());

            for (Element element : allLinks){
                Element chapterLink = element.select(".chapter-link a").first();

                String chapterName = chapterLink.select("p").first().text();

                ChapterIndex c = new ChapterIndex(
                        chapterName,
                        chapterLink.attr("href").replace(URL_BASE, ""),
                        chapterIndices.size());

                chapterIndices.add(c);
            }

            Collections.reverse(chapterIndices);

            for (int i = 0; i < chapterIndices.size(); i++) {
                chapterIndices.get(i).setSourceId(i);
            }

            return chapterIndices;
        } catch(IOException e){
            e.printStackTrace();
        }

        return chapterIndices;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        String url = "/latest-novels/1";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();


        try{
            Document document = Jsoup.connect(URL_BASE + url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".page-content-listing .page-listing-item .page-item-detail");

            for (Element e : novels){
                Element link = e.select("a").first();
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("data-src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        link.attr("title"),
                        link.attr("href").replace(URL_BASE, "")));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        String url = "https://reaperscans.com/?s=" + searchValue.replaceAll(" ", "+") + "&post_type=wp-manga";

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements novels = document.select(".c-tabs-item .row .c-image-hover a");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("data-src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.attr("title"),
                        e.attr("href").replace(URL_BASE , "")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return novelsArr;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = URL_BASE + chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();

            String title = document.select(".active").first().text();

            String rawChapter = document.html();

            cleanDocument(document);

            String chapterContent = document.select(".new-content").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new ReaperScansParser(ctx);
    }

    @Override
    protected String cleanChapter(String content) {
        return super.cleanChapter(content)
                .replaceAll("<br>", "\n")
                .replaceAll("<strong>", "")
                .replaceAll("</strong>", "")
                .replaceAll("<em>", "")
                .replaceAll("</em>", "")
                .replaceAll("<p>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("<p(.*?)>", "\n")
                .replaceAll("<div(.*?)>", "\n")
                .replaceAll("</div>", "\n")
                .replaceAll("<b(.*?)>", "")
                .replaceAll("</b>", "")
                .replaceAll("<span(.*?)>", "")
                .replaceAll("</span>", "")
                .replaceAll("\\n\\s+(.*?)", "\n\n")
                .trim();
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
