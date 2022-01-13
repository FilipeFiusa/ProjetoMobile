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

public class WuxiaBlogParser extends Parser {
    public WuxiaBlogParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://www.wuxia.blog/";
        SourceName = "WuxiaBlog";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_wuxiablog);
        language = Languages.ENGLISH;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();

            //Get the novel name
            String title = document.select("div h4").first().text();

            // Get novel description
            String description = document.select("[itemprop=\"description\"]")
                    .first()
                    .html()
                    .replaceAll("\\\\n", "\n")
                    .replaceAll("<p>", "\n")
                    .replaceAll("<h4>", "")
                    .replaceAll("</h4>", "")
                    .replaceAll("<strong>", "")
                    .replaceAll("</strong>", "")
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
            String author = document.select(".panel-body .col-lg-6").get(1).text().replace("Author:", "");

            //Get the novel image
            Element img = document.select("[itemprop=\"image\"]").get(1);
            java.lang.String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //ArrayList<ChapterIndex> allChapters = getAllChaptersIndex(document, novelLink);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource(SourceName);
            novelDetails.setNovelLink(novelLink);

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

            Elements allLinks = d.select("#chplist tr a");

            if(allLinks.isEmpty()){
                return chapterIndices;
            }

            for(Element e : allLinks){
                ChapterIndex c = new ChapterIndex(
                        e.text(),
                        e.attr("href"),
                        chapterIndices.size());

                c.setId(chapterIndices.size());
                chapterIndices.add(c);
            }

            if(chapterIndices.size() >= 100){
                String dataId = d.select("#more").first().attr("data-nid");
                System.out.println(dataId);
                System.out.println(URL_BASE + "temphtml/_tempChapterList_all_" + dataId + ".html");

                Document getOtherChaps = Jsoup.connect(URL_BASE + "temphtml/_tempChapterList_all_" + dataId + ".html").userAgent("Mozilla/5.0").get();
                Elements otherLinks = getOtherChaps.select("a");

                for(Element e : otherLinks){
                    ChapterIndex c = new ChapterIndex(
                            e.text(),
                            e.attr("href"),
                            chapterIndices.size());

                    c.setId(chapterIndices.size());
                    chapterIndices.add(c);
                }
            }

            Collections.reverse(chapterIndices);

            for(int i = 0; i < chapterIndices.size(); i++){
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
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();
        int count = 0;

        try{
            Document document = Jsoup.connect(URL_BASE)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".panel-body .row .media");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.attr("src").replace(URL_BASE, "");
                imgSrc = URL_BASE + imgSrc;

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        Objects.requireNonNull(e.select("h4").first()).text(),
                        Objects.requireNonNull(e.select("a").first()).attr("href")));

                count ++;
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String _searchValue) {
        boolean checkedFirst = false;
        int count = 0;

        String searchValue = removeSpaces(_searchValue);
        String url = "https://www.wuxia.blog/?search=";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        System.out.println(url + searchValue);

        try{
            url = url + searchValue;
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0").get();
            Elements novels = document.select("table tr");

            for (Element e : novels){
                if(count >= 10){
                    break;
                }

                if(!checkedFirst){
                    checkedFirst = true;
                    continue;
                }

                System.out.println(e.select(".xxxx a").text());
                System.out.println(e.select(".xxxx a").attr("href").replace(URL_BASE, ""));

                Element img = e.select("img").first();
                String imgSrc = img.attr("src").replace(URL_BASE, "");
                imgSrc = URL_BASE + imgSrc;

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        e.select(".xxxx a").text(),
                        e.select(".xxxx a").attr("href").replace(URL_BASE, "") ));

                count ++;
            }

            return novelsArr;
        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();

            String rawChapter = document.html();

            String title = document.select("h4").first().text();

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

            Element chapterElement = document.select(".article").first();

            chapterElement.select("button").remove();
            chapterElement.select("div").remove();
            chapterElement.select("span").remove();

            String chapterContent = chapterElement
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
                    .replaceAll("</span>", "")
                    .replaceAll("\r", "\n")
                    .replaceAll("\n", "\n")
                    .replaceAll("\n\n", "\n")
                    .replaceAll("\n\n\n", "\n")
                    .trim();

            chapterContent = cleanHTMLEntities(chapterContent);

            System.out.println(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new WuxiaBlogParser(ctx);
    }

    private String removeSpaces(String searchValue) {
        return searchValue.replaceAll(" ", "+");
    }

    private Bitmap getNovelImage(Element element) {
        Bitmap bitmap;

        try {
            Element img = element.select("img").first();
            String imgSrc = img.attr("src").replace(URL_BASE, "");
            InputStream input = new java.net.URL(URL_BASE + imgSrc).openStream();
            bitmap = BitmapFactory.decodeStream(input);

            return bitmap;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
}
