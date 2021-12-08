package com.example.mobileproject.model.parser.english;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.content.ContextCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NovelFullParser extends Parser {
    public NovelFullParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://novelfull.com";
        SourceName = "NovelFull";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_novelfull);
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();

            //Get the novel name
            String title = document.select(".title").first().text();

            // Get novel description
            String description = document.select(".desc-text")
                    .first()
                    .html()
                    .replaceAll("\\\\n", "\n")
                    .replaceAll("<p>", "\n")
                    .replaceAll("\" ", "")
                    .replaceAll(" \"", "")
                    .replaceAll("\' ", "")
                    .replaceAll(" \'", "")
                    .replaceAll("<p>", "\n")
                    .replaceAll("<br>", "\n")
                    .replaceAll("</br>", "\n")
                    .replaceAll("</p>", "\n")
                    .trim();

            // Get novel author
            String author = document.select(".info div").first().text().replace("Author:", "");

            //Get the novel image
            Element img = document.select(".book img").first();
            java.lang.String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //ArrayList<ChapterIndex> allChapters = getAllChaptersIndex(document, novelLink);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource("NovelFull");
            novelDetails.setNovelLink(novelLink);

            return novelDetails;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<ChapterIndex> getAllChaptersIndex(String novelLink) {
        // #list-chapter .row a
        ArrayList<ChapterIndex> chapterIndices = new ArrayList<>();
        Document d;

        try {
            Document document = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();
            String data_page = document.select(".last a").first().attr("data-page");

            Thread.sleep(1000);

            for(int i = 1; i <= (Integer.parseInt(data_page) + 1); i++){
                d = Jsoup.connect(URL_BASE + novelLink + "?page=" + i).userAgent("Mozilla/5.0").get();

                Elements allLinks = d.select("#list-chapter .row a");

                for(Element e : allLinks){
                    ChapterIndex c = new ChapterIndex(e.text(), e.attr("href"), chapterIndices.size());
                    c.setId(chapterIndices.size());
                    chapterIndices.add(c);

                    Thread.sleep(1000);
                }
            }

            return chapterIndices;
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        java.lang.String url = "https://novelfull.com/hot-novel";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".archive .row h3 a");

            for (Element e : novels){
                Document getImage = Jsoup.connect(URL_BASE + e.attr("href"))
                        .userAgent("Mozilla/5.0").get();
                novelsArr.add(new NovelDetailsMinimum(getNovelImage(getImage), e.text(), e.attr("href") ));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String _searchValue) {
        String searchValue = removeSpaces(_searchValue);
        String url = "https://novelfull.com/search?keyword=";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            url = url + searchValue;
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0").get();
            Elements novels = document.select(".archive .row h3 a");

            for (Element e : novels){
                Document getImage = Jsoup.connect(URL_BASE + e.attr("href")).userAgent("Mozilla/5.0").get();
                novelsArr.add(new NovelDetailsMinimum(getNovelImage(getImage), e.text(), e.attr("href") ));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = URL_BASE + chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();

            String title = document.select(".chapter-text").first().text();

            document.select("script").remove();
            document.select("ins").remove();
            document.select("h1").remove();
            document.select("h2").remove();
            document.select("h3").remove();
            document.select("h4").remove();
            document.select("h5").remove();
            document.select(".google-auto-placed").remove();
            document.select(".ap_container").remove();
            document.select(".ads").remove();
            document.select(".ads-holder").remove();
            document.select(".ads-middle").remove();
            document.select("[align]").remove();
            removeComments(document);

            String chapterContent = document.select("#chapter-content")
                    .first()
                    .html()
                    .replaceAll("  ", "")
                    .replaceAll("\r", "\n")
                    .replaceAll("\n", "\n")
                    .replaceAll("\n\n", "\n")
                    .replaceAll("\n\n\n", "\n")
                    .replaceAll("<p></p>\n", "")
                    .replaceAll("<p></p>\r", "")
                    .replaceAll("<p></p>", "")
                    .replaceAll("<p>", "")
                    .replaceAll("</p>\n", "\n\n")
                    .replaceAll("</p>\r", "\n\n")
                    .replaceAll("</p>", "\n\n")
                    .replaceAll("<em>", "")
                    .replaceAll("</em>", "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&ZeroWidthSpace;", " ")
                    .replaceAll("&zeroWidthSpace;", " ")
                    .replaceAll("<br>", "")
                    .replaceAll("<hr>", "")
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
                    .trim();

            //chapterContent = chapterContent
              //      .replaceAll("\\n\\n", "\n")
             //       .replaceAll("\\n\\n\\n", "\n")
              //      .replaceAll("\\n\\n\\n\\n", "\n")
               //     .replaceAll("\\n\\n\\n\\n\\n", "\n")
                //    .replaceAll("\\n\\n\\n\\n\\n\\n", "\n")
                //    .replaceAll("\\n\\n\\n\\n\\n\\n\\n", "\n"); */

            System.out.println(chapterContent);

            ChapterContent content = new ChapterContent(chapterContent, title, chapterUrl);

            return content;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new NovelFullParser(ctx);
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }

    private Bitmap getNovelImage(Document document) {
        Bitmap bitmap;

        try {
            Element img = document.select(".book img").first();
            java.lang.String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            bitmap = BitmapFactory.decodeStream(input);

            return bitmap;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

}
