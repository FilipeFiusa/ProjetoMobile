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
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class RoyalRoadParser extends Parser {
    public RoyalRoadParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://www.royalroad.com";
        SourceName = "RoyalRoad";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_royalroad);
        language = Languages.ENGLISH;
    }


    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();

            //Get the novel name
            String title = document.select("h1").first().text();

            // Get novel description
            String description = document.select("[property='description']")
                    .first()
                    .html()
                    .replaceAll("\\\\n", "\n")
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
                    .trim();

            // Cleaning Description
            description = cleanHTMLEntities(description);

            // Get status
            String status = document.select(".margin-bottom-10 span").get(1).text();

            // Get novel author
            String author = document.select("[property='author'] [property='name']").first().text();

            //Get the novel image
            Element img = document.select("[property='image']").first();
            java.lang.String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //ArrayList<ChapterIndex> allChapters = getAllChaptersIndex(document, novelLink);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource(SourceName);
            novelDetails.setNovelLink(novelLink);

            if(status.equals("ONGOING")){
                novelDetails.setStatus(1);
            }else if(status.equals("COMPLETED")){
                novelDetails.setStatus(2);
            }else{
                novelDetails.setStatus(1);
            }

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

            Elements allLinks = d.select("#chapters tbody tr");

            for (Element element : allLinks){
                String chapterName = element.select("td").first().text();


                ChapterIndex c = new ChapterIndex(
                        chapterName,
                        element.attr("data-url"),
                        chapterIndices.size());

                c.setId(chapterIndices.size());
                chapterIndices.add(c);
            }

            Collections.reverse(chapterIndices);

            return chapterIndices;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        int count = 0;
        java.lang.String url = "/fictions/rising-stars";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(URL_BASE + url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select("#result .fiction-list-item");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        Objects.requireNonNull(e.select(".fiction-title a").first()).text(),
                        Objects.requireNonNull(e.select(".fiction-title a").first()).attr("href")));

                count++;
                if(count > 20){
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        int count = 0;
        String _searchValue = removeSpaces(searchValue);
        String url = "https://www.royalroad.com/fictions/search?title=";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();
        url = url + _searchValue;

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".search-container .fiction-list .fiction-list-item");

            for (Element e : novels) {
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        Objects.requireNonNull(e.select(".fiction-title a").first()).text(),
                        Objects.requireNonNull(e.select(".fiction-title a").first()).attr("href")));

                count++;
                if (count > 10) {
                    break;
                }

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

            String title = document.select("h1").first().text();

            String rawChapter = document.html();

            String chapterContent = document.select(".chapter-content").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new RoyalRoadParser(ctx);
    }

    @Override
    protected String cleanChapter(String content){
        return super.cleanChapter(content)
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
                .replaceAll("</p>\n", "\n")
                .replaceAll("</p>\r", "\n")
                .replaceAll("</p>", "\n")
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
                .replaceAll("\n\n\n", "\n\n")
                .trim()
                ;
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }
}
