package com.example.mobileproject.model.parser.english;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

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

public class GenesisTlParser extends Parser {
    public GenesisTlParser(Context ctx) {
        super(ctx);

        urlBase = "https://genesistls.com";
        SourceName = "Genesis Translations";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_genesistl);
        language = Languages.ENGLISH;
        sourceType = 1;
    }


    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(urlBase + novelLink).userAgent("Mozilla/5.0").get();

            //Get the novel name
            String title = document.select(".entry-title").first().text();

            // Get novel description
            String description = document.select(".entry-content").first().html();
            description = cleanDescription(description);

            // Get status
            //String status = document.select(".info div").get(4).text();

            // Get novel author
            String author = document.select(".spe span").get(1).text().replace("Author:", "");

            //Get the novel image
            Element img = document.select(".thumb img").first();
            java.lang.String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //ArrayList<ChapterIndex> allChapters = getAllChaptersIndex(document, novelLink);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource(SourceName);
            novelDetails.setNovelLink(novelLink);

//            if(status.equals("Status:Completed") || status.equals("Status: Completed")){
//                novelDetails.setStatus(2);
//            }else{
                novelDetails.setStatus(1);
            //}

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
            Document document = Jsoup.connect(urlBase + novelLink).userAgent("Mozilla/5.0").get();

            Elements allLinks = document.select(".eplister ul li a");

            for(Element e : allLinks){

                if(e.select(".epl-title img").size() > 0 || e.select(".epl-title").text().contains("\uD83D\uDD12") ){
                    continue;
                }


                ChapterIndex c = new ChapterIndex(
                        e.select(".epl-title").first().text(),
                        e.attr("href"),
                        chapterIndices.size()
                );
                c.setId(chapterIndices.size());
                chapterIndices.add(c);
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
            Document document = Jsoup.connect(urlBase + "/series/")
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".listupd article");

            for (Element e : novels){
                java.lang.String imgSrc = e.select("img").first().absUrl("src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.select(".ntitle").text(),
                        e.select("a").first().attr("href").replace(urlBase, "") ));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        return new ArrayList<>();
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        try {
            Document document = Jsoup.connect(chapterUrl).userAgent("Mozilla/5.0").get();

            String rawChapter = document.html();

            String title = document.select(".cat-series").first().text();

            String chapterContent = document.select(".epcontent").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);

        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new GenesisTlParser(ctx);
    }
}
