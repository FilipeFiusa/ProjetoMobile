package com.example.mobileproject.model.parser.english;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NovelFullParser implements Parser {
    public final java.lang.String URL_BASE = "https://novelfull.com";

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + novelLink).get();

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
            Document document = Jsoup.connect(URL_BASE + novelLink).get();
            String data_page = document.select(".last a").first().attr("data-page");

            Thread.sleep(1000);

            for(int i = 1; i <= (Integer.parseInt(data_page) + 1); i++){
                d = Jsoup.connect(URL_BASE + novelLink + "?page=" + i).get();

                Elements allLinks = d.select("#list-chapter .row a");

                for(Element e : allLinks){
                    ChapterIndex c = new ChapterIndex(e.text(), e.attr("href"));
                    c.setId(chapterIndices.size());
                    chapterIndices.add(c);
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
            Document document = Jsoup.connect(url).get();
            Elements novels = document.select(".archive .row h3 a");

            for (Element e : novels){
                Document getImage = Jsoup.connect(URL_BASE + e.attr("href")).get();
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
            Document document = Jsoup.connect(url).get();
            Elements novels = document.select(".archive .row h3 a");

            for (Element e : novels){
                Document getImage = Jsoup.connect(URL_BASE + e.attr("href")).get();
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
            Document document = Jsoup.connect(URL).get();

            //Get the novel name
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

            // Get novel description
            String chapterContent = document.select("#chapter-content")
                    .first()
                    .html()
                    .replaceAll("<p>", "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("<p></p>", "")
                    .replaceAll(" \"", "\"")
                    .replaceAll("\n ", "\n")
                    .replaceAll("<p>", "\n")
                    .replaceAll("<br>", "")
                    .replaceAll("<i>", "")
                    .replaceAll("</i>", "")
                    .replaceAll("<div>", "")
                    .replaceAll("</div>", "")
                    .replaceAll("<strong>", "")
                    .replaceAll("</strong>", "")
                    .replaceAll("</br>", "\n")
                    .replaceAll("</p>", "\n")
                    .trim();

            ChapterContent content = new ChapterContent(chapterContent, title, chapterUrl);

            return content;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
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

    private void removeComments(Node node) {
        for (int i = 0; i < node.childNodeSize();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }

}
