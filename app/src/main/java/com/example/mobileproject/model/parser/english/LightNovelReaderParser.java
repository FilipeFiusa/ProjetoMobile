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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class LightNovelReaderParser extends Parser {
    public LightNovelReaderParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://lightnovelreader.org/";
        SourceName = "LightNovelReader";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_lightnovelreader);
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
            String title = document.select(".novel-title").first().text();

            // Get novel description
            String description = document.select(".empty-box").get(1).html();

            // Cleaning Description
            description = cleanDescription(description);

            // Get status

            // Get novel author
            String author = document.select(".novels-detail .novels-detail-right-in-right")
                    .get(5).text();

            //Get the novel image
            Element img = document.select(".novels-detail .novels-detail-left img").first();
            java.lang.String imgSrc = img.absUrl("src");
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
            d = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();

            Elements allLinks = d.select(".cm-tabs-content li a");

            for (Element element : allLinks){
                String chapterName = element.text();


                ChapterIndex c = new ChapterIndex(
                        chapterName,
                        element.attr("href").replace(URL_BASE, ""),
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
        java.lang.String url = "/ranking/top-rated/1";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(URL_BASE + url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".category-items ul").get(0).children();

            for (Element e : novels){
                Element link = e.select("a").first();
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        link.text(),
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

        String requestURL = "https://lightnovelreader.org/search/autocomplete?dataType=json&query=" + searchValue.replaceAll(" ", "%20");
        try {
            InputStream is = new URL(requestURL).openStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);

            JSONArray jArray = json.getJSONArray("results");

            for(int i = 0; i < jArray.length(); i++) {
                JSONObject o = jArray.getJSONObject(i);
                System.out.println(o.toString());


                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        o.get("image").toString(),
                        o.get("original_title").toString(),
                        o.get("link").toString())
                );
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = URL_BASE + chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();

            String title = document.select(".chapter-player-options-right .cm-dropdown [selected]").first().text();

            String rawChapter = document.html();

            cleanDocument(document);

            String chapterContent = document.select("#chapterText").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new LightNovelReaderParser(ctx);
    }

    @Override
    protected String cleanChapter(String content) {
        return super.cleanChapter(content)
                .replaceAll("<center(.*?\\n\\s*)|Sponsored Content|<div(.*\\n\\s*)<script(.*\\n\\s*)|</center>", "")
                .replaceAll("<span(.*)</span>", "")
                .replaceAll("<br>", "\n")
                .replaceAll("<p>", "\n")
                .replaceAll("<em>", "\n")
                .replaceAll("</em>", "\n")
                .replaceAll("<p(.*?)>", "\n")
                .replaceAll("<strong>", "")
                .replaceAll("</strong>", "")
                .replaceAll("</p>", "\n")
                .replaceAll("<div(.*?)>", "\n")
                .replaceAll("</div>", "\n")
                .replaceAll("<a(.*?)>", "\n")
                .replaceAll("</a>", "\n")
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
