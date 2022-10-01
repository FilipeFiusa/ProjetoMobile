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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class ReaperScansParser extends Parser {
    public ReaperScansParser(Context ctx) {
        super(ctx);

        urlBase = "https://reaperscans.com";
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
            Document document = Jsoup.connect(urlBase +  novelLink).userAgent("Mozilla/5.0").get();
            cleanDocument(document);

            //Get the novel name
            String title = document.select(".post-title h1").first().text();

            // Get novel description
            String description = document.select(".summary__content").first().html();

            // Cleaning Description
            description = cleanDescription(description);

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
            d = Jsoup.connect(urlBase + novelLink).userAgent("Mozilla/5.0").get();

            d.select(".premium-block").remove();
            Elements allLinks = d.select(".version-chap li");


            System.out.println(allLinks.html());

            for (Element element : allLinks){
                Element chapterLink = element.select(".chapter-link a").first();

                String chapterName = chapterLink.select("p").first().text();

                ChapterIndex c = new ChapterIndex(
                        chapterName,
                        chapterLink.attr("href").replace(urlBase, ""),
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
            Document document = Jsoup.connect(urlBase + url)
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
                        link.attr("href").replace(urlBase, "")));
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
                        e.attr("href").replace(urlBase, "")));
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

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
