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
import java.util.Objects;

public class FoxaholicParser extends Parser {

    public FoxaholicParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://www.foxaholic.com";
        SourceName = "Foxaholic";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_foxaholic);
        language = Languages.ENGLISH;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + "/novel/" + novelLink).userAgent("Mozilla/5.0").get();
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
                    .replaceAll("\' ", "")
                    .replaceAll(" \'", "")
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
            d = Jsoup.connect(URL_BASE + "/novel/" + novelLink).userAgent("Mozilla/5.0").get();

            Elements allLinks = d.select(".listing-chapters_wrap .wp-manga-chapter a");

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
        java.lang.String url = "/novel/";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(URL_BASE + url)
                    .userAgent("Mozilla/5.0")
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
                        link.attr("href").replace(URL_BASE + url, "")));
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
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".search-wrap .row .tab-thumb a");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("data-src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.attr("title"),
                        e.attr("href").replace(URL_BASE + "/novel/", "")));
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

    @Override
    protected String cleanChapter(String content){
        return cleanHTMLEntities(content)
                .replaceAll("  ", "")
                .replaceAll("\r", "\n")
                .replaceAll("\n", "\n")
                .replaceAll("\n\n", "\n")
                .replaceAll("\n\n\n", "\n")
                .replaceAll("<p>&nbsp;</p>", "")
                .replaceAll("<p> </p>", "")
                .replaceAll("<p></p>\n", "")
                .replaceAll("<p></p>\r", "")
                .replaceAll("<p></p>", "")
                .replaceAll("<p>", "")
                .replaceAll("</p>\n", "\n\n")
                .replaceAll("</p>\r", "\n\n")
                .replaceAll("</p>", "\n\n")
                .replaceAll("<em>", "")
                .replaceAll("</em>", "")
                .replaceAll("&ZeroWidthSpace;", " ")
                .replaceAll("&zeroWidthSpace;", " ")
                .replaceAll("<br>", "")
                .replaceAll("<i>", "")
                .replaceAll("</i>", "")
                .replaceAll("<strong>", "")
                .replaceAll("</strong>", "")
                .replaceAll("</br>", "")
                .replaceAll("<div></div>\n", "")
                .replaceAll("<div></div>\r", "")
                .replaceAll("<div></div>", "")
                .replaceAll("<div>\r", "")
                .replaceAll("<div>\n", "")
                .replaceAll("<div>", "")
                .replaceAll("</div>\r", "")
                .replaceAll("</div>\n", "")
                .replaceAll("</div>", "")
                .replaceAll("<span>", "")
                .replaceAll("<span(.*?)>", "")
                .replaceAll("</span>", "")
                .replaceAll("<div(.*?)></div>", "")
                .replaceAll("<div(.*?)>", "")
                .replaceAll("</div>", "")
                .replaceAll("<b(.*?)>", "")
                .replaceAll("</b>", "")
                .replaceAll("<img(.*?)>", "")
                .replaceAll("</img>", "")
                .replaceAll("<a(.*?)>", "")
                .replaceAll("</a>", "")
                .replaceAll("<p(.*?)>", "")
                .replaceAll("</p>", "")
                .replaceAll("<!--(.*?)-->", "")
                .replaceAll("\n\n\n", "\n\n")
                .trim();
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }
}
