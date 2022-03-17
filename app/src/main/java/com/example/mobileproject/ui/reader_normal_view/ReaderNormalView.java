package com.example.mobileproject.ui.reader_normal_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.Chapter;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelCleaner;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.util.FontFactory;

import java.util.ArrayList;

public class ReaderNormalView {

    private FrameLayout layout;
    private ReaderActivity ctx;

    private TextView chapterName;
    private TextView chapterContentView;
    private ScrollView scrollView;

    private TextView bottomChapterNameView;

    private int textViewType = 1;

    public PageViewController pageViewController;
    public ScrollViewController scrollViewController;

    private NovelReaderController nrc;
    private String currentSourceName;

    private final ArrayList<NovelCleaner> novelCleaners;

    public ReaderNormalView(FrameLayout layout, ReaderActivity ctx, NovelReaderController nrc,
                            String currentSourceName, TextView bottomChapterNameView,
                            ArrayList<NovelCleaner> novelCleaners, int textViewType) {
        this.layout = layout;
        this.ctx = ctx;
        this.nrc = nrc;
        this.currentSourceName = currentSourceName;
        this.textViewType = textViewType;

        if(bottomChapterNameView != null){
            this.bottomChapterNameView = bottomChapterNameView;
        }else{
            this.bottomChapterNameView =  ctx.findViewById(R.id.chapter_name_bottom);

        }
        this.novelCleaners = novelCleaners;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ctx.getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if(textViewType == 1){
            setUpScrollViewType();
        }else if(textViewType == 2){
            setUpPageViewType();
        }
    }

    private void setUpScrollViewType(){
        FrameLayout container = layout.findViewById(R.id.normal_reader_container);
        container.removeAllViews();
        FrameLayout chapterContainer = (FrameLayout) ctx.getLayoutInflater()
                .inflate(R.layout.reader_normal_view_scroll_view, container, false);
        scrollViewController = new ScrollViewController(chapterContainer, ctx, nrc);
        container.addView(chapterContainer);
    }

    private void setUpPageViewType(){
        FrameLayout container = layout.findViewById(R.id.normal_reader_container);
        container.removeAllViews();
        FrameLayout chapterContainer = (FrameLayout) ctx.getLayoutInflater()
                .inflate(R.layout.reader_normal_view_page_view, container, false);
        pageViewController = new PageViewController(ctx, nrc, chapterContainer);
        container.addView(chapterContainer);
    }

    public void changeReaderSettings(float font_size, String font_name, String font_color, String background_color){
        SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(background_color != null){
            LinearLayout container = layout.findViewById(R.id.reader_container);
            container.setBackgroundColor(Color.parseColor(background_color));
            editor.putString("background_color", background_color);
        }
        TextView chapterContentView = (TextView) layout.findViewById(R.id.chapter_content);
        TextView chapterTitleView = (TextView) layout.findViewById(R.id.chapter_name);

        if(font_size != 0){
            chapterContentView.setTextSize(font_size);
            chapterTitleView.setTextSize(font_size + 15);
            editor.putFloat("font_size", font_size);
        }

        if(font_name != null){
            chapterContentView.setTypeface(new FontFactory().GetFont(font_name, ctx));
            chapterTitleView.setTypeface(new FontFactory().GetFont(font_name, ctx));
            editor.putString("font_name", font_name);
        }

        if(font_color != null) {
            chapterContentView.setTextColor(Color.parseColor(font_color));
            chapterTitleView.setTextColor(Color.parseColor(font_color));
            editor.putString("font_color", font_color);
        }

        editor.apply();
    }

    public void setChapterContent(int direction){
        if(textViewType == 1){
/*            ChapterContent currentChapter = nrc.getCurrentChapter().getChapterContent();

            chapterName.setText(currentChapter.getChapterName());
            chapterContentView.setText(currentChapter.getChapterContent());

            scrollView.fullScroll(ScrollView.FOCUS_UP);
            scrollView.pageScroll(ScrollView.FOCUS_UP);
            scrollView.smoothScrollTo(0,0);*/
        }else if (textViewType == 2 && pageViewController != null){
            pageViewController.addChapter(direction);
        }
    }

    public void initializeReaderView(){
        InitializeNovelReaderController initializer = new InitializeNovelReaderController();
        initializer.execute(nrc);
    }

    public void loadChapter(int direction){
        if(textViewType == 1){
            GetChapterContentScrollView scrollViewLoader = new GetChapterContentScrollView();
            scrollViewLoader.execute();
        }else if(textViewType == 2){
            GetChapterContentPageView pageViewLoader = new GetChapterContentPageView();
            pageViewLoader.execute(direction);
        }
    }


    private class InitializeNovelReaderController extends AsyncTask<NovelReaderController, Void, Boolean> {

        private boolean hasInternet = true;

        @Override
        protected Boolean doInBackground(NovelReaderController... novelReaderControllers) {
            NovelReaderController nrc = novelReaderControllers[0];
            DBController db = new DBController(ctx);

            if (!isNetworkAvailable(ctx)){
                hasInternet = false;
            }

            ParserInterface parser = ParserFactory.getParserInstance(currentSourceName,ctx);
            if(parser == null){
                return false;
            }

            //Checking if previous chapter is downloaded.
            //If it is, then update chapter content
            // If it isnt, then download it
            Chapter previousChapter = nrc.getCurrentPreviousChapter();
            if(previousChapter.exist()){
                previousChapter.setChapterContent(checkIfChapterAlreadyDownloaded(previousChapter.getChapterIndex()));
                if ( (previousChapter.getChapterContent() == null) || (previousChapter.isChapterEmpty()) ){
                    ChapterContent chapterContent;

                    if(hasInternet){
                        chapterContent = parser.getChapterContent(previousChapter.getChapterIndex().getChapterLink());
                        chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));

                        previousChapter.setChapterContent(chapterContent);
                        db.setChapterContent(previousChapter.getChapterIndex().getId(), chapterContent.getChapterContent(), chapterContent.getRawChapter());
                    }else{
                        previousChapter.setExist(false);
                        previousChapter.setInvalidType(2);
                    }
                }
            }

            //Checking if current chapter is downloaded. If it is, then update chapter content
            //If it is, then update chapter content
            // If it isnt, then download it
            Chapter currentChapter = nrc.getCurrentChapter();
            if(currentChapter.exist()){
                currentChapter.setChapterContent(checkIfChapterAlreadyDownloaded(currentChapter.getChapterIndex()));
                if ( (currentChapter.getChapterContent() == null) || (currentChapter.isChapterEmpty()) ){
                    ChapterContent chapterContent;

                    if(hasInternet){
                        chapterContent = parser.getChapterContent(currentChapter.getChapterIndex().getChapterLink());
                        chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));

                        currentChapter.setChapterContent(chapterContent);
                        db.setChapterContent(currentChapter.getChapterIndex().getId(), chapterContent.getChapterContent(), chapterContent.getRawChapter());
                    }else{

                        currentChapter.setExist(false);
                        currentChapter.setInvalidType(2);
                    }
                }
            }

            //Checking if next chapter is downloaded. If it is, then update chapter content
            //If it is, then update chapter content
            // If it isnt, then download it
            Chapter nextChapter = nrc.getCurrentNextChapter();
            if(nextChapter.exist()){
                nextChapter.setChapterContent(checkIfChapterAlreadyDownloaded(nextChapter.getChapterIndex()));
                if ( (nextChapter.getChapterContent() == null) || (nextChapter.isChapterEmpty()) ){
                    ChapterContent chapterContent;

                    if(hasInternet){
                        chapterContent = parser.getChapterContent(nextChapter.getChapterIndex().getChapterLink());
                        chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));

                        nextChapter.setChapterContent(chapterContent);
                        db.setChapterContent(nextChapter.getChapterIndex().getId(), chapterContent.getChapterContent(), chapterContent.getRawChapter());
                    }else{
                        nextChapter.setExist(false);
                        nextChapter.setInvalidType(2);
                    }
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                if(textViewType == 1){
                    if(hasInternet){
                        scrollViewController.setChapterContent(nrc.getCurrentChapter().getChapterContent());
                    }else{
                        Toast.makeText(ctx, "Sem internet", Toast.LENGTH_SHORT).show();
                    }
                }else if (textViewType == 2 && pageViewController != null){
                    pageViewController.updateChapters();
                }

                if(hasInternet){
                    bottomChapterNameView.setText(nrc.getCurrentChapter().getChapterContent().getChapterName());
                }else{
                    bottomChapterNameView.setText(nrc.getCurrentChapter().getChapterIndex().getChapterName());
                }
            }
        }

        public boolean isNetworkAvailable(final Context context) {
            final ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            return (networkInfo != null && networkInfo.isConnected());
        }

        private String CleanChapter(String chapterContent){
            String cleanedChapter = chapterContent;

            if(novelCleaners == null || novelCleaners.isEmpty()){
                return chapterContent;
            }

            for(NovelCleaner cleaner : novelCleaners){
                if(!cleaner.isActive()) continue;

                cleanedChapter = cleanedChapter.replaceAll(cleaner.getFlag(), cleaner.getReplacement());
            }

            return cleanedChapter;
        }

        private ChapterContent checkIfChapterAlreadyDownloaded(ChapterIndex chapter){
            ChapterContent chapterContent = null;

            if(chapter != null && (chapter.getDownloaded().equals("yes") || chapter.getDownloaded().equals("downloading"))){
                DBController db = new DBController(ctx);
                chapterContent = db.getChapter(chapter.getId());

                if(!chapterContent.getChapterContent().isEmpty()){
                    chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));
                    return chapterContent;
                }
            }

            return null;
        }
    }

    private class GetChapterContentPageView extends AsyncTask<Integer, Void, ChapterContent> {
        private boolean hasInternet = true;
        private int direction;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ChapterContent doInBackground(Integer... direction) {
            this.direction = direction[0];
            DBController db = new DBController(ctx);
            Chapter chapter = null;

            SystemClock.sleep(200);

            if(direction[0] == 1){
                chapter = nrc.getCurrentNextChapter();
            }else if(direction[0] == 2){
                chapter = nrc.getCurrentPreviousChapter();
            }else if(direction[0] == 3){
                chapter = nrc.getCurrentChapter();
            }

            if(chapter == null || chapter.getChapterIndex() == null){
                return null;
            }

            if(chapter.getChapterContent() != null){
                return chapter.getChapterContent();
            }

            ChapterContent chapterContent = checkIfChapterAlreadyDownloaded(chapter.getChapterIndex());

            if(chapterContent != null){
                chapter.setChapterContent(chapterContent);
                return chapterContent;
            }

            if (!isNetworkAvailable(ctx)){
                hasInternet = false;

                return null;
            }

            ParserInterface parser = ParserFactory.getParserInstance(currentSourceName, ctx);
            if(parser == null){
                return null;
            }

            chapterContent = parser.getChapterContent(chapter.getChapterIndex().getChapterLink());
            chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));

            db.setChapterContent(chapter.getChapterIndex().getId(), chapterContent.getChapterContent(), chapterContent.getRawChapter());

            chapter.setChapterContent(chapterContent);

            if(nrc.getCurrentNextChapter().equals(chapter)){
                nrc.getCurrentNextChapter().setChapterContent(chapterContent);
            }

            if(nrc.getCurrentPreviousChapter().equals(chapter)){
                nrc.getCurrentPreviousChapter().setChapterContent(chapterContent);
            }

            return chapterContent;
        }

        @Override
        protected void onPostExecute(ChapterContent chapterContent) {
            super.onPostExecute(chapterContent);

            if(chapterContent == null && !hasInternet ){
                Toast.makeText(ctx, "Sem internet", Toast.LENGTH_SHORT).show();
                return;
            }

            if(chapterContent == null || chapterContent.getChapterContent().isEmpty()){
                Toast.makeText(ctx, "Falha ao acessar", Toast.LENGTH_SHORT).show();
                return;
            }


            pageViewController.addChapter(direction);
            bottomChapterNameView.setText(chapterContent.getChapterName());
        }

        public boolean isNetworkAvailable(final Context context) {
            final ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            return (networkInfo != null && networkInfo.isConnected());
        }

        private String CleanChapter(String chapterContent){
            String cleanedChapter = chapterContent;

            if(novelCleaners == null || novelCleaners.isEmpty()){
                return chapterContent;
            }

            for(NovelCleaner cleaner : novelCleaners){
                if(!cleaner.isActive()) continue;

                cleanedChapter = cleanedChapter.replaceAll(cleaner.getFlag(), cleaner.getReplacement());
            }

            return cleanedChapter;
        }

        private ChapterContent checkIfChapterAlreadyDownloaded(ChapterIndex chapter){
            ChapterContent chapterContent = null;

            if(chapter != null && (chapter.getDownloaded().equals("yes") || chapter.getDownloaded().equals("downloading"))){
                DBController db = new DBController(ctx);
                chapterContent = db.getChapter(chapter.getId());

                if(!chapterContent.getChapterContent().isEmpty()){
                    chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));
                    return chapterContent;
                }
            }

            return chapterContent;
        }
    }

    private class GetChapterContentScrollView extends AsyncTask<Void, Void, ChapterContent> {
        private boolean hasInternet = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ChapterContent doInBackground(Void... voids) {
            DBController db = new DBController(ctx);
            Chapter chapter = nrc.getCurrentChapter();

            SystemClock.sleep(200);


            if(chapter == null || chapter.getChapterIndex() == null){
                return null;
            }

            if(chapter.getChapterContent() != null){
                return chapter.getChapterContent();
            }

            ChapterContent chapterContent = checkIfChapterAlreadyDownloaded(chapter.getChapterIndex());

            if(chapterContent != null){
                chapter.setChapterContent(chapterContent);
                return chapterContent;
            }

            if (!isNetworkAvailable(ctx)){
                hasInternet = false;

                return null;
            }

            ParserInterface parser = ParserFactory.getParserInstance(currentSourceName, ctx);
            if(parser == null){
                return null;
            }

            chapterContent = parser.getChapterContent(chapter.getChapterIndex().getChapterLink());
            chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));

            chapter.setChapterContent(chapterContent);
            db.setChapterContent(chapter.getChapterIndex().getId(), chapterContent.getChapterContent(), chapterContent.getRawChapter());

            return chapterContent;
        }

        @Override
        protected void onPostExecute(ChapterContent chapterContent) {
            super.onPostExecute(chapterContent);

            if(chapterContent == null && !hasInternet ){
                Toast.makeText(ctx, "Sem internet", Toast.LENGTH_SHORT).show();
                return;
            }

            if(chapterContent == null || chapterContent.getChapterContent().isEmpty()){
                Toast.makeText(ctx, "Falha ao acessar", Toast.LENGTH_SHORT).show();
                return;
            }

            scrollViewController.setChapterContent(chapterContent);
            bottomChapterNameView.setText(chapterContent.getChapterName());
        }

        public boolean isNetworkAvailable(final Context context) {
            final ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            return (networkInfo != null && networkInfo.isConnected());
        }

        private String CleanChapter(String chapterContent){
            String cleanedChapter = chapterContent;

            if(novelCleaners == null || novelCleaners.isEmpty()){
                return chapterContent;
            }

            for(NovelCleaner cleaner : novelCleaners){
                if(!cleaner.isActive()) continue;

                cleanedChapter = cleanedChapter.replaceAll(cleaner.getFlag(), cleaner.getReplacement());
            }

            return cleanedChapter;
        }

        private ChapterContent checkIfChapterAlreadyDownloaded(ChapterIndex chapter){
            ChapterContent chapterContent = null;

            if(chapter != null && (chapter.getDownloaded().equals("yes") || chapter.getDownloaded().equals("downloading"))){
                DBController db = new DBController(ctx);
                chapterContent = db.getChapter(chapter.getId());

                if(!chapterContent.getChapterContent().isEmpty()){
                    chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));
                    return chapterContent;
                }
            }

            return chapterContent;
        }
    }

}
