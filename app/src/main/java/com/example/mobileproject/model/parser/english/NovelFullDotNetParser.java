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

public class NovelFullDotNetParser extends Parser {
    public NovelFullDotNetParser(Context ctx) {
        super(ctx);

        urlBase = "https://novelfull.net";
        SourceName = "NovelFull.net";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_novelfulldotnet);
        language = Languages.ENGLISH;
        sourceType = 2;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(urlBase + novelLink).userAgent("Mozilla/5.0").get();

            //Get the novel name
            String title = document.select(".title").first().text();

            // Get novel description
            String description = document.select(".desc-text").first().html();
            description = cleanDescription(description);

            // Get status
            String status = document.select(".info div").get(3).text();

            // Get novel author
            String author = document.select(".info div").first().text().replace("Author:", "");

            //Get the novel image
            Element img = document.select(".book img").first();
            String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //ArrayList<ChapterIndex> allChapters = getAllChaptersIndex(document, novelLink);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource("NovelFull");
            novelDetails.setNovelLink(novelLink);

            if(status.equals("Status:Completed") || status.equals("Status: Completed")){
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
        // #list-chapter .row a
        ArrayList<ChapterIndex> chapterIndices = new ArrayList<>();
        Document d;

        try {
            Document document = Jsoup.connect(urlBase + novelLink).userAgent("Mozilla/5.0").get();
            String data_page = document.select(".last a").first().attr("data-page");

            for(int i = 1; i <= (Integer.parseInt(data_page) + 1); i++){
                d = Jsoup.connect(urlBase + novelLink + "?page=" + i).userAgent("Mozilla/5.0").get();

                Elements allLinks = d.select("#list-chapter .row a");

                for(Element e : allLinks){
                    ChapterIndex c = new ChapterIndex(e.text(), e.attr("href"), chapterIndices.size());
                    c.setId(chapterIndices.size());
                    chapterIndices.add(c);

                }

                Thread.sleep(500);
            }

            lastPageSearched = (Integer.parseInt(data_page) + 1);

            return chapterIndices;
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

        return chapterIndices;
    }

    protected ArrayList<ChapterIndex> getPaginatedChapters(String novelLink, int page){
        ArrayList<ChapterIndex> chapterIndices = new ArrayList<>();
        Document d;

        lastPageSearched = page;

        try {
            while (true){
                ArrayList<ChapterIndex> tempChapterIndices = new ArrayList<>();

                Thread.sleep(500);

                d = Jsoup.connect(urlBase + novelLink + "?page=" + lastPageSearched).userAgent("Mozilla/5.0").get();

                Elements allLinks = d.select("#list-chapter .row a");

                for(Element e : allLinks){
                    ChapterIndex c = new ChapterIndex(e.text(), e.attr("href"), chapterIndices.size());
                    c.setId(chapterIndices.size());
                    tempChapterIndices.add(c);
                }

                for(ChapterIndex currentChapterIndex : tempChapterIndices){
                    for (int i = 0; i < chapterIndices.size(); i++) {
                        if(currentChapterIndex.getChapterLink().equals(chapterIndices.get(i).getChapterLink())){
                            lastPageSearched = lastPageSearched - 1;
                            return chapterIndices;
                        }
                    }
                }

                chapterIndices.addAll(tempChapterIndices);
                lastPageSearched = lastPageSearched + 1;
            }

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

        return chapterIndices;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        String url = urlBase + "/hot-novel";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".archive .row h3 a");

            for (Element e : novels){
                Document getImage = Jsoup.connect(urlBase + e.attr("href"))
                        .userAgent("Mozilla/5.0").get();

                Element img = getImage.select(".book img").first();
                String imgSrc = img.absUrl("src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.text(),
                        e.attr("href") ));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String _searchValue) {
        String searchValue = removeSpaces(_searchValue);
        String url = urlBase + "/search?keyword=";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            url = url + searchValue;
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0").get();
            Elements novels = document.select(".archive .row h3 a");

            for (Element e : novels){
                Document getImage = Jsoup.connect(urlBase + e.attr("href")).userAgent("Mozilla/5.0").get();

                Element img = getImage.select(".book img").first();
                String imgSrc = img.absUrl("src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.text(),
                        e.attr("href") ));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = urlBase + chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();

            String rawChapter = document.html();

            String title = document.select(".chapter-text").first().text();

            String chapterContent = document.select("#chapter-content").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);

        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new NovelFullDotNetParser(ctx);
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }


}
