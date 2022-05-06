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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WoopreadParser extends Parser {
    public WoopreadParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://woopread.com";
        SourceName = "WoopRead";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_woopread);
        language = Languages.ENGLISH;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();
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
        String html = "";
        ArrayList<ChapterIndex> novelsArr = new ArrayList<>();


        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .build();

        String url = URL_BASE + novelLink + "/ajax/chapters/";
        url = url.replaceAll("//", "/");

        Request request = new Request.Builder()
                .header("User-Agent", "Mozilla/5.0")
                .url(url)
                .post(body)
                .build();


        try (Response response = client.newCall(request).execute()) {
            String temp = response.body().string();
            html = temp;

            if(html.equals("")){
                return novelsArr;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document document = Jsoup.parse(html);

        // Novel has more than one season/volumn. Example: Great mage
        if(document.select(".volumns").size() > 0){
            Elements seasons = document.select(".volumns .parent");

            for(Element season : seasons){
                ArrayList<ChapterIndex> tempNovelArr = new ArrayList<>();

                Elements seasonChapters = season.select(".free-chap");

                for(Element chapter : seasonChapters){
                    Element chapterLink = chapter.select("a").first();

                    ChapterIndex c = new ChapterIndex(
                            chapterLink.text(),
                            chapterLink.attr("href").replace(URL_BASE, ""),
                            tempNovelArr.size());

                    tempNovelArr.add(c);
                }

                Collections.reverse(tempNovelArr);

                for (int i = 0; i < tempNovelArr.size(); i++) {
                    tempNovelArr.get(i).setSourceId(novelsArr.size() + 1);
                    novelsArr.add(tempNovelArr.get(i));
                }
            }

            return novelsArr;
        }

        // Its a normal novel
        if(document.select(".no-volumn").size() > 0){
            ArrayList<ChapterIndex> tempNovelArr = new ArrayList<>();
            Elements chapters = document.select(".no-volumn .free-chap");

            for(Element chapter : chapters){
                Element chapterLink = chapter.select("a").first();

                ChapterIndex c = new ChapterIndex(
                        chapterLink.text(),
                        chapterLink.attr("href").replace(URL_BASE, ""),
                        tempNovelArr.size());

                tempNovelArr.add(c);
            }

            Collections.reverse(tempNovelArr);

            for (int i = 0; i < tempNovelArr.size(); i++) {
                tempNovelArr.get(i).setSourceId(novelsArr.size() + 1);
                novelsArr.add(tempNovelArr.get(i));
            }

            return novelsArr;
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        java.lang.String url = "/novellist/";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(URL_BASE + url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".c-tabs-item .row .page-item-detail");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("data-src");

                Element novelName = e.select(".item-summary h3 a").first();

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        novelName.text(),
                        novelName.attr("href").replace(URL_BASE, "")));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        String _searchValue = removeSpaces(searchValue);
        String url = "https://woopread.com/?s=" + _searchValue + "&post_type=wp-manga";
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
                        e.attr("href").replace(URL_BASE, "")));
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

            String title = document.select(".breadcrumb .active").first().text();

            String rawChapter = document.html();

            cleanDocument(document);

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
        return new WoopreadParser(ctx);
    }

    @Override
    protected String cleanChapter(String content){
        return cleanHTMLEntities(content)
                .replaceAll("  ", "")
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
                .replaceAll("<!--(.*?)-->", "")
                .replaceAll("<style(.*?)>(.*?)</style>", "")
                .replaceAll("<style (.*?)>(.*?)</style>", "")
                .replaceAll("<h1>", "")
                .replaceAll("</h1>", "\n")
                .replaceAll("<h2>", "")
                .replaceAll("</h2>", "\n")
                .replaceAll("<h3>", "")
                .replaceAll("</h3>", "\n")
                .replaceAll("<h4>", "")
                .replaceAll("</h4>", "\n")
                .replaceAll("<h5>", "")
                .replaceAll("</h5>", "\n")
                .replaceAll("<h6>", "")
                .replaceAll("</h6>", "\n")
                .replaceAll("<b(.*?)>", "")
                .replaceAll("</b>", "")
                .replaceAll("<img(.*?)>", "")
                .replaceAll("</img>", "")
                .replaceAll("<a(.*?)>", "")
                .replaceAll("</a>", "")
                .replaceAll("<p(.*?)>", "")
                .replaceAll("</p>", "")
                .replaceAll("\r", "\n")
                .replaceAll("   \n", "\n")
                .replaceAll("  \n", "\n")
                .replaceAll(" \n", "\n")
                .replaceAll("\n\n", "\n")
                .replaceAll("\n\n\n", "\n")
                .replaceAll("\n\n\n", "\n\n")
                .trim();
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }
}
