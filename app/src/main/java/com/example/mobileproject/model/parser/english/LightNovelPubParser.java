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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LightNovelPubParser extends Parser {
    public LightNovelPubParser(Context ctx) {
        super(ctx);

        URL_BASE = "https://www.lightnovelpub.com";
        SourceName = "LightNovelPub";
        Icon = ContextCompat.getDrawable(ctx, R.drawable.favicon_lightnovelpub);
        language = Languages.ENGLISH;
    }

    @Override
    public NovelDetails getNovelDetails(String novelLink) {
        NovelDetails novelDetails;

        try {
            //Connect to website
            Document document = Jsoup.connect(URL_BASE + novelLink).userAgent("Mozilla/5.0").get();

            //Get the novel name
            String title = document.select(".novel-info .main-head h1").first().text();

            // Get novel description
            String description = document.select(".content")
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
            String status = document.select(".header-stats span").get(3).text();

            // Get novel author
            String author = document.select(".author").first().text().replace("Author:", "");

            //Get the novel image
            Element img = document.select(".cover img").first();
            java.lang.String imgSrc = img.absUrl("data-src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //ArrayList<ChapterIndex> allChapters = getAllChaptersIndex(document, novelLink);

            // Instantiating a novelDetails object to return
            novelDetails = new NovelDetails(bitmap, title, description, author);
            novelDetails.setSource(SourceName);
            novelDetails.setNovelLink(novelLink);

            if(status.equals("Completed Status")){
                novelDetails.setStatus(2);
            }else {
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
            for(int i = 1; i <= 1000; i++){
                d = Jsoup.connect(URL_BASE + novelLink + "/chapters/page-" + i).userAgent("Mozilla/5.0").get();

                Elements allLinks = d.select(".chapter-list li a");

                if(allLinks.isEmpty()){
                    return chapterIndices;
                }

                for(Element e : allLinks){
                    if(e.select(".chapter-title").first().text().contains("(empty)")){
                        continue;
                    }

                    ChapterIndex c = new ChapterIndex(
                            e.attr("title"),
                            e.attr("href"),
                            chapterIndices.size());

                    c.setId(chapterIndices.size());
                    chapterIndices.add(c);
                }
            }

            return chapterIndices;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> getHotNovels() {
        java.lang.String url = "/genre";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        try{
            Document document = Jsoup.connect(URL_BASE + url)
                    .userAgent("Mozilla/5.0")
                    .get();
            Elements novels = document.select(".novel-item");

            for (Element e : novels){
                Element img = e.select("img").first();
                String imgSrc = img.absUrl("data-src");

                novelsArr.add(new NovelDetailsMinimum(
                        novelsArr.size(),
                        imgSrc,
                        Objects.requireNonNull(e.select(".novel-title a").first()).text(),
                        Objects.requireNonNull(e.select(".novel-title a").first()).attr("href")));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return novelsArr;
    }

    @Override
    public ArrayList<NovelDetailsMinimum> searchNovels(String searchValue) {
        return new ArrayList<>();
        /*
        String html = "";
        ArrayList<NovelDetailsMinimum> novelsArr = new ArrayList<>();

        JSONObject postJsonObject = new JSONObject();
        try {
            postJsonObject.put("inputContent", searchValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, postJsonObject.toString());

        String url = URL_BASE + "/lnwsearchlive";

        Request request = new Request.Builder()
                .header("User-Agent", "Mozilla/5.0")
                .addHeader("lnrequestverifytoken", "CfDJ8LL1tUsY83pMlvlZAmXeR3RPrB7mXqQIsT7aW_P07D1tabNa7nc-cT7ToRC6ip2YzwaXcUlv5c7yD9JsDldBZfIrNRS5oDSY5Ob19Kv1rAJk7x6Cfri1La1C0R729T_hz2LAcd_BsaJtn4lA34XLonA")
                .url(url)
                .post(body)
                .build();


        try (Response response = client.newCall(request).execute()) {
            String temp = response.body().string();
            System.out.println(temp);

            JsonObject jsonObject = new JsonParser().parse(temp).getAsJsonObject();
            html = jsonObject.get("resultview").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(html.equals("")){
            return novelsArr;
        }

        Document document = Jsoup.parse(html);
        Elements novels = document.select("li");

        for (Element e : novels){
            Element img = e.select("img").first();
            String imgSrc = img.absUrl("src");

            novelsArr.add(new NovelDetailsMinimum(
                    novelsArr.size(),
                    imgSrc,
                    e.select("h4").first().text()
                            .replace("&#x27;", "'")
                            .replace("&#x2019;", "'"),
                    e.select("a").attr("href").replace("\\&quot;", "")
            ));
        }

        return novelsArr;*/
    }

    @Override
    public ChapterContent getChapterContent(String chapterUrl) {
        String URL = URL_BASE + chapterUrl;

        try {
            Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0").get();

            String title = document.select(".chapter-title").first().text();

            String rawChapter = document.html();

            cleanDocument(document);

            String chapterContent = document.select("#chapter-container").first().html();

            chapterContent = cleanChapter(chapterContent);

            return new ChapterContent(chapterContent, title, chapterUrl, rawChapter);
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ParserInterface getParserInstance() {
        return new LightNovelPubParser(ctx);
    }

    @Override
    protected void cleanDocument(Document document){
        super.cleanDocument(document);

        document.select("h1").remove();
        document.select("h2").remove();
        document.select("h3").remove();
        document.select("h4").remove();
        document.select("h5").remove();
        document.select("[align]").remove();
    }

    @Override
    protected String cleanChapter(String content){
        return super.cleanChapter(content)
                .replaceAll("\\<p><strong><strong>(.*?)\\<\\/strong><\\/strong><\\/p>", "")
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
                .trim()
                ;
    }

/*    private String CleanSpecificChapterContent(String chapterContent){
        return chapterContent
                .replaceAll("<p><strong><strong>Try the lightnovelpub.com plataform_for the most advanced_reading experience.</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Try the lightnovelpub.com plataform_for the most advanced_reading experience.</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Try the lightnovelpub.com plataform_for the most advanced_reading experience</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Follow new_episodes on the lightnovelpub.com plataform.</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Follow new_episodes on the lightnovelpub.com plataform</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Updated_at lightnovelpub.com</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>New_chapters are published on lightnovelpub.com</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Visit lightnovelpub.com for a better_reading experience</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>Follow current_novel on lightnovelpub.com</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>You can_find the rest of this_content on the lightnovelpub.com plataform.</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>You can_find the rest of this_content on the lightnovelpub.com plataform</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>The source of this_chapter; lightnovelpub.com</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>The latest_episodes are on_the lightnovelpub.com website.</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>The latest_episodes are on_the lightnovelpub.com website</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>New novel_chapters are published here: lightnovelpub.com</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>This_content is taken from lightnovelpub.com</strong></strong></p>", "")
                .replaceAll("<p><strong><strong>The most up-to-date novels are published_here &gt; lighnovelpub.com</strong></strong></p>", "")

                .replaceAll("Try the lightnovelpub.com plataform_for the most advanced_reading experience.</strong></strong></p>", "")
                .replaceAll("Try the lightnovelpub.com plataform_for the most advanced_reading experience.", "")
                .replaceAll("Try the lightnovelpub.com plataform_for the most advanced_reading experience", "")
                .replaceAll("Follow new_episodes on the lightnovelpub.com plataform.", "")
                .replaceAll("Follow new_episodes on the lightnovelpub.com plataform", "")
                .replaceAll("Updated_at lightnovelpub.com", "")
                .replaceAll("New_chapters are published on lightnovelpub.com", "")
                .replaceAll("Visit lightnovelpub.com for a better_reading experience", "")
                .replaceAll("Follow current_novel on lightnovelpub.com", "")
                .replaceAll("You can_find the rest of this_content on the lightnovelpub.com plataform.", "")
                .replaceAll("You can_find the rest of this_content on the lightnovelpub.com plataform", "")
                .replaceAll("The source of this_chapter; lightnovelpub.com", "")
                .replaceAll("The latest_episodes are on_the lightnovelpub.com website.", "")
                .replaceAll("The latest_episodes are on_the lightnovelpub.com website", "")
                .replaceAll("New novel_chapters are published here: lightnovelpub.com", "")
                .replaceAll("This_content is taken from lightnovelpub.com", "")
                .replaceAll("The most up-to-date novels are published_here &gt; lighnovelpub.com", "");
    }*/

    private Bitmap getNovelImage(Element element) {
        Bitmap bitmap;

        try {
            Element img = element.select("img").first();
            String imgSrc = img.absUrl("data-src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            bitmap = BitmapFactory.decodeStream(input);

            return bitmap;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap getNovelImage2(Element element) {
        Bitmap bitmap;

        try {
            Element img = element.select("img").first();
            String imgSrc = img.absUrl("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            bitmap = BitmapFactory.decodeStream(input);

            return bitmap;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

}
