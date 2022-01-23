package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelCleaner;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.services.CheckUpdateService;
import com.example.mobileproject.services.receivers.CheckUpdatesNDReceiver;
import com.example.mobileproject.services.receivers.CheckUpdatesRAReceiver;
import com.example.mobileproject.services.receivers.DownloadRAReceiver;
import com.example.mobileproject.ui.reader_cleansers.ReaderCleaner;
import com.example.mobileproject.ui.reader_normal_view.ReaderNormalView;
import com.example.mobileproject.ui.reader_settings.ReaderSettings;
import com.example.mobileproject.ui.reader_web_view.ReaderWebViewController;
import com.example.mobileproject.util.FontFactory;
import com.example.mobileproject.util.ServiceHelper;

import java.util.ArrayList;


public class ReaderActivity extends AppCompatActivity {
    private final ReaderActivity ctx = this;

    private NovelReaderController nrc;
    private GetChapterContent getChapterContent;
    Button previousButton;
    Button nextButton;

    private RecyclerView mRecyclerView;
    private ReaderChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private final ArrayList<Integer> chaptersReadied = new ArrayList<>();
    private ArrayList<NovelCleaner> novelCleaners = null;

    private SeekBar mSeekBar;
    private TextView chapterProgress;

    Animation animTranslateIn;
    Animation animTranslateOut;
    Animation animTranslateBottomIn;
    Animation animTranslateBottomOut;
    Animation animTranslateSideIn;
    Animation animTranslateSideOut;

    public ReaderSettings readerSettings;
    public ReaderCleaner readerCleaner;

    public ReaderWebViewController webViewController;
    public ReaderNormalView normalViewController;

    private ChapterContent currentChapterContent;

    private String novelName;
    private String sourceName;

    private Thread checkForServiceStartedThread;

    private DownloadRAReceiver downloadReceiver;
    private CheckUpdatesRAReceiver updatesReceiver;

    private int readerViewType; // 1- Normal View / 2- ReaderView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        //setReaderUserPreferences();

        animTranslateIn = AnimationUtils.loadAnimation(this, R.anim.translate_in);
        animTranslateOut = AnimationUtils.loadAnimation(this, R.anim.translate_out);
        animTranslateBottomIn = AnimationUtils.loadAnimation(this, R.anim.translate_botton_in);
        animTranslateBottomOut = AnimationUtils.loadAnimation(this, R.anim.translate_bottom_out);
        animTranslateSideIn = AnimationUtils.loadAnimation(this, R.anim.translate_side_in);
        animTranslateSideOut = AnimationUtils.loadAnimation(this, R.anim.translate_side_out);


        Intent i = getIntent();
        String chapterLink = i.getStringExtra("chapterLink");
        readerViewType = i.getIntExtra("readerViewType", 1);
        sourceName = i.getStringExtra("sourceName");
        novelName = i.getStringExtra("novelName");
        nrc = (NovelReaderController) i.getSerializableExtra("NovelReaderController");
        nrc.setStartedChapter(chapterLink);

        mRecyclerView = findViewById(R.id.reader_menu_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ReaderChaptersAdapter(nrc, chapterLink, this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);


        if(readerViewType == 1){
            setUpNormalView();
        }else if(readerViewType == 2){
            setUpWebView();
        }

        setUpMenu();

        updatesReceiver = new CheckUpdatesRAReceiver(new Handler(), this);
        downloadReceiver = new DownloadRAReceiver(new Handler(), this);
        mAdapter.setDownloadReceiver(downloadReceiver);

        checkForServiceStartedThread = new Thread(new CheckForUpdateServiceRunningTask());
        checkForServiceStartedThread.start();

        GetCleaners getCleaners = new GetCleaners();
        getCleaners.execute();

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(nrc.getCurrentChapter());
    }

    private void setUpNormalView(){
        LinearLayout container = (LinearLayout) findViewById(R.id.reader_container);
        container.removeAllViews();
        FrameLayout inflatedLayout= (FrameLayout) getLayoutInflater()
                .inflate(R.layout.reader_normal_view, container, false);
        normalViewController = new ReaderNormalView(inflatedLayout, ReaderActivity.this, currentChapterContent);
        container.addView(inflatedLayout);

        readerViewType = 1;
    }

    private void setUpWebView(){
        LinearLayout container = (LinearLayout) findViewById(R.id.reader_container);
        container.removeAllViews();
        CoordinatorLayout inflatedLayout= (CoordinatorLayout) getLayoutInflater()
                .inflate(R.layout.reader_web_view, container, false);
        webViewController = new ReaderWebViewController(inflatedLayout, ReaderActivity.this, nrc.getCurrentChapter(), sourceName, currentChapterContent);
        container.addView(inflatedLayout);

        readerViewType = 2;
    }

    private void setUpMenu(){
        Intent i = getIntent();
        novelName = i.getStringExtra("novelName");

        ImageButton returnButton = (ImageButton) findViewById(R.id.reader_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("readiedChapters", chaptersReadied);
                data.putExtra("currentReaderViewType", readerViewType);
                setResult(RESULT_OK,data);

                finish();
            }
        });

        TextView novelNameTextView = (TextView) findViewById(R.id.reader_novel_name);
        novelNameTextView.setText(novelName);

        ImageButton nextButton = (ImageButton) findViewById(R.id.reader_menu_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNextChapter();
            }
        });

        ImageButton previousButton = (ImageButton) findViewById(R.id.reader_menu_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreviousChapter();
            }
        });

        TextView sideMenuNovelName = (TextView) findViewById(R.id.reader_side_menu_name);
        sideMenuNovelName.setText(novelName);

        chapterProgress = (TextView) findViewById(R.id.reader_seekbar_progress);
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        mSeekBar = (SeekBar) findViewById(R.id.novel_progress);
        mSeekBar.setProgress(0);
        mSeekBar.setMax(nrc.getSize() - 1);
        mSeekBar.setProgress(nrc.getPosition());

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                chapterProgress.setText(new StringBuilder().append(i + 1).append("/").append(nrc.getSize()).toString());
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("Go to - ", String.valueOf(progress - 1));

                ChapterIndex c = nrc.goToChapter(progress);

                getChapterContent = new GetChapterContent();
                getChapterContent.execute(c);
            }
        });

        ImageButton openChaptersMenu = (ImageButton) findViewById(R.id.open_chapter_side_menu);
        openChaptersMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
                FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

                hideSystemUI();

                topMenu.startAnimation(animTranslateOut);
                bottomMenu.startAnimation(animTranslateBottomOut);

                topMenu.setVisibility(View.GONE);
                bottomMenu.setVisibility(View.GONE);

                mRecyclerView.scrollToPosition(mAdapter.currentReadingPosition);
                sideMenu.setVisibility(View.VISIBLE);
                sideMenu.startAnimation(animTranslateSideIn);
            }
        });

        ImageButton openReaderSettings = (ImageButton) findViewById(R.id.open_reader_settings);
        openReaderSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);

                topMenu.startAnimation(animTranslateOut);
                bottomMenu.startAnimation(animTranslateBottomOut);
                topMenu.setVisibility(View.GONE);
                bottomMenu.setVisibility(View.GONE);

                RelativeLayout container = (RelativeLayout) findViewById(R.id.reader_activity);
                FrameLayout inflatedLayout= (FrameLayout) getLayoutInflater()
                        .inflate(R.layout.reader_settings, container, false);
                readerSettings = new ReaderSettings(inflatedLayout, ReaderActivity.this);
                container.addView(inflatedLayout);
            }
        });

        ImageButton openUserCleansers = (ImageButton) findViewById(R.id.open_user_cleansers);
        openUserCleansers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);

                topMenu.startAnimation(animTranslateOut);
                bottomMenu.startAnimation(animTranslateBottomOut);
                topMenu.setVisibility(View.GONE);
                bottomMenu.setVisibility(View.GONE);

                RelativeLayout container = (RelativeLayout) findViewById(R.id.reader_activity);
                FrameLayout inflatedLayout= (FrameLayout) getLayoutInflater()
                        .inflate(R.layout.reader_user_cleaners, container, false);
                readerCleaner = new ReaderCleaner(inflatedLayout, ReaderActivity.this, novelCleaners, novelName, sourceName);
                container.addView(inflatedLayout);
            }
        });

        ImageButton reloadChapterButton = (ImageButton) findViewById(R.id.reload_chapter_content);
        reloadChapterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChapterContent = new GetChapterContent();
                getChapterContent.execute(nrc.getCurrentChapter());
            }
        });

        ImageButton toggleReaderViewType = (ImageButton) findViewById(R.id.open_chapter_on_web_view);
        if(readerViewType == 1){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_public_24);
        }else if(readerViewType == 2){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_library_books_24);
        }
        toggleReaderViewType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(readerViewType == 1){
                    setUpWebView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_library_books_24);
                }else if(readerViewType == 2){
                    setUpNormalView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_public_24);
                }

                toggleReaderMenu();

                ChangeReaderViewType changeReaderViewType = new ChangeReaderViewType();
                changeReaderViewType.execute(readerViewType);
            }
        });
    }

    public void toggleReaderMenu(){
        FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
        FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
        FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

        if(readerSettings != null){
            FrameLayout temp = readerSettings.getFrameLayout();
            RelativeLayout container = (RelativeLayout) findViewById(R.id.reader_activity);
            container.removeView(temp);
            readerSettings = null;

            return;
        }

        if(sideMenu.getVisibility() == View.VISIBLE){
            sideMenu.startAnimation(animTranslateSideOut);
            sideMenu.setVisibility(View.GONE);

            return;
        }

        if (topMenu.getVisibility() == View.GONE){
            showSystemUI();

            topMenu.setVisibility(View.VISIBLE);
            bottomMenu.setVisibility(View.VISIBLE);

            topMenu.startAnimation(animTranslateIn);
            bottomMenu.startAnimation(animTranslateBottomIn);
        }else{
            hideSystemUI();

            topMenu.startAnimation(animTranslateOut);
            bottomMenu.startAnimation(animTranslateBottomOut);

            topMenu.setVisibility(View.GONE);
            bottomMenu.setVisibility(View.GONE);
        }
    }

    public void changeReaderSettings(float font_size, String font_name, String font_color, String background_color){
        if(normalViewController != null){
            normalViewController.changeReaderSettings(font_size, font_name, font_color, background_color);
        }
    }

    public void getPreviousChapter(){
        if(getChapterContent.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }

        ChapterIndex previous = nrc.getPreviousChapter();

        if(previous == null){
            Toast.makeText(this, "Não tem capitulo anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(previous);
    }

    public void getNextChapter() {
        if(getChapterContent.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }
        int currentChapter = nrc.getCurrentChapter().getId();

        if(currentChapter != -1 && !chaptersReadied.contains(currentChapter)){
            chaptersReadied.add(currentChapter);
            DBController db = new DBController(this);
            db.setChapterAsReaded(currentChapter);
        }

        ChapterIndex next = nrc.getNextChapter();

        if(next == null){
            Toast.makeText(this, "Não tem capitulo posterior", Toast.LENGTH_SHORT).show();
            return;
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(next);
    }

    public void goTo(ChapterIndex c){
        FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);
        int currentPosition = nrc.getPosition();

        if(sideMenu.getVisibility() == View.VISIBLE){
            sideMenu.startAnimation(animTranslateSideOut);
            sideMenu.setVisibility(View.GONE);
        }

        mSeekBar.setProgress(currentPosition);
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());


        getChapterContent = new GetChapterContent();
        getChapterContent.execute(c);
    }

    public void updateChapterList(String novelName, String novelSource){
        if(novelName.equals(this.novelName) && novelSource.equals(this.sourceName)){
            System.out.println("Updating");

            UpdateChaptersList updateChaptersList = new UpdateChaptersList();
            updateChaptersList.execute();

            new Thread(new CheckForDownloadServiceRunningTask()).start();
        }
    }

    public void ChapterDownloaded(int chapterId){
        mAdapter.ChapterDownloaded(chapterId);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("readiedChapters", chaptersReadied);
        data.putExtra("currentReaderViewType", readerViewType);
        setResult(RESULT_OK,data);

        finish();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private class CheckForUpdateServiceRunningTask implements Runnable {

        @Override
        public void run() {
            boolean isServiceRunning = false;
            while (!Thread.currentThread().isInterrupted()) {
                isServiceRunning = ServiceHelper.isMyServiceRunning(ctx, CheckUpdateService.class);

                if (isServiceRunning) {
                    Intent serviceIntent = new Intent(ctx, CheckUpdateService.class);
                    serviceIntent.putExtra("receiver3", (Parcelable) updatesReceiver);
                    ctx.startService(serviceIntent);

                    SystemClock.sleep(10000);
                }

                SystemClock.sleep(1000);
            }
        }
    }

    private class CheckForDownloadServiceRunningTask implements Runnable {

        @Override
        public void run() {
            boolean isServiceRunning = false;
            while (!isServiceRunning){
                if(Thread.currentThread().isInterrupted())
                    break;

                isServiceRunning = ServiceHelper.isMyServiceRunning(ctx, DownloaderService.class);

                if(isServiceRunning){
                    System.out.println("CreatingDownloadReceiver");
                    Intent serviceIntent = new Intent(ctx, DownloaderService.class);
                    serviceIntent.putExtra("receiver2", (Parcelable) downloadReceiver);
                    ctx.startService(serviceIntent);
                }

                SystemClock.sleep(1000);
            }
        }
    }

    private class GetChapterContent extends AsyncTask<ChapterIndex, Void, ChapterContent> {
        private TextView chapterNameBottom;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            chapterNameBottom = (TextView) findViewById(R.id.chapter_name_bottom);
        }

        @Override
        protected ChapterContent doInBackground(ChapterIndex... chapter) {
            ChapterContent chapterContent;

            if(chapter[0].getDownloaded().equals("yes") || chapter[0].getDownloaded().equals("downloading")){
                DBController db = new DBController(ReaderActivity.this);
                chapterContent = db.getChapter(chapter[0].getId());

                if(!chapterContent.getChapterContent().isEmpty()){
                    chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));
                    return chapterContent;
                }
            }

            ParserInterface parser = ParserFactory.getParserInstance(sourceName,ReaderActivity.this);
            if(parser == null){
                return null;
            }

            chapterContent = parser.getChapterContent(chapter[0].getChapterLink());

            chapterContent.setChapterContent(CleanChapter(chapterContent.getChapterContent()));

            return chapterContent;
        }

        @Override
        protected void onPostExecute(ChapterContent chapterContent) {
            super.onPostExecute(chapterContent);

            if(chapterContent == null || chapterContent.getChapterContent().isEmpty()){
                Toast.makeText(ReaderActivity.this, "Falha ao acessar", Toast.LENGTH_SHORT).show();
                return;
            }

            currentChapterContent = chapterContent;


            if(readerViewType == 1){
                normalViewController.setChapterContent(chapterContent);
            }else if(readerViewType == 2){
                webViewController.goToAnotherChapter(nrc.getCurrentChapter(), chapterContent);
            }

            chapterNameBottom.setText(chapterContent.getChapterName());
        }

        private String CleanChapter(String chapterContent){
            String cleanedChapter = chapterContent;

            while (novelCleaners == null){
                SystemClock.sleep(100);
            }

            for(NovelCleaner cleaner : novelCleaners){
                if(!cleaner.isActive()) continue;

                cleanedChapter = cleanedChapter.replaceAll(cleaner.getFlag(), cleaner.getReplacement());
            }

            return cleanedChapter;
        }
    }

    private class GetCleaners extends AsyncTask<Void, Void, ArrayList<NovelCleaner>>{

        @Override
        protected ArrayList<NovelCleaner> doInBackground(Void... voids) {
            DBController db = new DBController(ReaderActivity.this);

            return db.getCleanerConnections(novelName, sourceName);
        }

        @Override
        protected void onPostExecute(ArrayList<NovelCleaner> cleaners) {
            super.onPostExecute(cleaners);

            novelCleaners = cleaners;
        }
    }

    private class ChangeReaderViewType extends AsyncTask<Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... integers) {
            DBController db = new DBController(ReaderActivity.this);

            db.changeReaderViewType(novelName, sourceName, integers[0]);

            return null;
        }
    }

    private class UpdateChaptersList extends AsyncTask<Void, Void, ArrayList<ChapterIndex>>{
        @Override
        protected ArrayList<ChapterIndex> doInBackground(Void... voids) {
            DBController db = new DBController(ctx);
            ArrayList<ChapterIndex> chapters = db.getChaptersFromANovel(novelName, sourceName);
            return chapters;
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndices) {
            super.onPostExecute(chapterIndices);

            mAdapter.updateChapterList(chapterIndices);
        }
    }
}

