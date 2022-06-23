package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.Chapter;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelCleaner;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.UserReaderPreferences;
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
import com.example.mobileproject.util.SystemUiHelper;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javassist.bytecode.analysis.Frame;


public class ReaderActivity extends AppCompatActivity {
    private final ReaderActivity ctx = this;

    private NovelReaderController nrc;

    private RecyclerView mRecyclerView;
    private ReaderChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private final ArrayList<Integer> chaptersReadied = new ArrayList<>();
    private final ArrayList<Integer> chaptersDownloaded = new ArrayList<>();
    private ArrayList<NovelCleaner> novelCleaners = null;

    private SeekBar mSeekBar;
    private TextView chapterProgress;

    private Animation animTranslateIn;
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
    private Thread checkForDownloaderServiceStarted;

    private DownloadRAReceiver downloadReceiver;
    private CheckUpdatesRAReceiver updatesReceiver;

    private UserReaderPreferences userReaderPreferences;

    private int readerViewType; // 1- Scroll View / 2- Web View / 3- Page View
    private int novelType;

    public TextView chapterNameBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SystemUiHelper.hideSystemUI(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        //setReaderUserPreferences();

        animTranslateIn = AnimationUtils.loadAnimation(this, R.anim.translate_in);
        animTranslateOut = AnimationUtils.loadAnimation(this, R.anim.translate_out);
        animTranslateBottomIn = AnimationUtils.loadAnimation(this, R.anim.translate_botton_in);
        animTranslateBottomOut = AnimationUtils.loadAnimation(this, R.anim.translate_bottom_out);
        animTranslateSideIn = AnimationUtils.loadAnimation(this, R.anim.translate_side_in);
        animTranslateSideOut = AnimationUtils.loadAnimation(this, R.anim.translate_side_out);

        userReaderPreferences = new UserReaderPreferences(this);


        Intent i = getIntent();
        String chapterLink = i.getStringExtra("chapterLink");
        readerViewType = i.getIntExtra("readerViewType", 1);
        novelType = i.getIntExtra("novelType", 1);
        sourceName = i.getStringExtra("sourceName");
        novelName = i.getStringExtra("novelName");
        nrc = (NovelReaderController) i.getSerializableExtra("NovelReaderController");

        if(chapterLink.equals("epub")){
            int sourceId = i.getIntExtra("sourceId", 1);
            nrc.setStartedChapter(sourceId);
        }else{
            nrc.setStartedChapter(chapterLink);
        }

        mRecyclerView = findViewById(R.id.reader_menu_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ReaderChaptersAdapter(nrc, chapterLink, this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);


        if(readerViewType == 1 || readerViewType == 3){
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
    }

    private void loadCurrentChapter(){
        if((readerViewType == 1 || readerViewType == 3) && normalViewController != null){
            normalViewController.initializeReaderView();
        }else if (readerViewType == 2 && webViewController != null) {

        }
    }

    private void reloadChapter(){

    }

    private void loadChapter(){
        if((readerViewType == 1 || readerViewType == 3) && normalViewController != null){
            normalViewController.initializeReaderView();
        }else if (readerViewType == 2 && webViewController != null) {

        }
    }

    private void loadChapter(int direction){
        if((readerViewType == 1 || readerViewType == 3) && normalViewController != null){
            normalViewController.loadChapter(direction);
        }else if (readerViewType == 2 && webViewController != null) {

        }
    }

    private void setUpNormalView(){
        LinearLayout container = (LinearLayout) findViewById(R.id.reader_container);
        container.removeAllViews();
        FrameLayout inflatedLayout= (FrameLayout) getLayoutInflater().inflate(R.layout.reader_normal_view, container, false);

        if(readerViewType == 1){
            normalViewController = new ReaderNormalView(inflatedLayout, ReaderActivity.this, nrc,
                    sourceName, chapterNameBottom, novelCleaners, 1, userReaderPreferences, novelType);
        }else if (readerViewType == 3){
            normalViewController = new ReaderNormalView(inflatedLayout, ReaderActivity.this, nrc,
                    sourceName, chapterNameBottom, novelCleaners, 2, userReaderPreferences, novelType);
        }
        container.addView(inflatedLayout);
    }

    private void setUpWebView(){
        LinearLayout container = (LinearLayout) findViewById(R.id.reader_container);
        container.removeAllViews();
        CoordinatorLayout inflatedLayout= (CoordinatorLayout) getLayoutInflater()
                .inflate(R.layout.reader_web_view, container, false);
        webViewController = new ReaderWebViewController(inflatedLayout, ReaderActivity.this, nrc.getCurrentChapter().getChapterIndex(), sourceName, currentChapterContent);
        container.addView(inflatedLayout);
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
                data.putExtra("downloadedChapters", chaptersDownloaded);
                data.putExtra("currentReaderViewType", readerViewType);
                setResult(RESULT_OK,data);

                checkForServiceStartedThread.interrupt();
                if(checkForDownloaderServiceStarted != null)
                    checkForDownloaderServiceStarted.interrupt();

                finish();
            }
        });

        TextView novelNameTextView = (TextView) findViewById(R.id.reader_novel_name);
        novelNameTextView.setText(novelName);

        ImageButton nextButton = (ImageButton) findViewById(R.id.reader_menu_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(normalViewController != null && normalViewController.pageViewController != null){
                    normalViewController.pageViewController.menuChangePage();
                }

                getNextChapter();
            }
        });

        ImageButton previousButton = (ImageButton) findViewById(R.id.reader_menu_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(normalViewController != null && normalViewController.pageViewController != null){
                    normalViewController.pageViewController.menuChangePage();
                }

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
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("Go to - ", String.valueOf(progress - 1));

                goToChapter(progress);
            }
        });

        ImageButton openChaptersMenu = (ImageButton) findViewById(R.id.open_chapter_side_menu);
        openChaptersMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
                FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

                SystemUiHelper.hideSystemUI(ReaderActivity.this);

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

        View closeMenu = findViewById(R.id.close_menu);
        closeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleReaderMenu();
            }
        });

        ImageButton reloadChapterButton = (ImageButton) findViewById(R.id.reload_chapter_content);
        reloadChapterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadChapter();
            }
        });

        ImageButton toggleReaderViewType = (ImageButton) findViewById(R.id.open_chapter_on_web_view);
/*        if(readerViewType == 1){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_public_24);
        }else if(readerViewType == 2){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_menu_book_24);
        }else if(readerViewType == 3){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_library_books_24);
        }*/
        if(readerViewType == 1){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_menu_book_24);
        }else if(readerViewType == 3){
            toggleReaderViewType.setImageResource(R.drawable.ic_baseline_library_books_24);
        }

        toggleReaderViewType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                if(readerViewType == 1){
                    readerViewType = 2;
                    setUpWebView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_menu_book_24);
                }else if(readerViewType == 2){
                    readerViewType = 3;
                    setUpNormalView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_library_books_24);
                }else if(readerViewType == 3){
                    readerViewType = 1;
                    setUpNormalView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_public_24);
                }*/

                if(readerViewType == 1){
                    readerViewType = 3;
                    setUpNormalView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_menu_book_24);
                }else if(readerViewType == 3){
                    readerViewType = 1;
                    setUpNormalView();
                    toggleReaderViewType.setImageResource(R.drawable.ic_baseline_library_books_24);
                }

                toggleReaderMenu();
                loadCurrentChapter();

                ChangeReaderViewType changeReaderViewType = new ChangeReaderViewType();
                changeReaderViewType.execute(readerViewType);
            }
        });

        chapterNameBottom = (TextView) findViewById(R.id.chapter_name_bottom);
    }

    public void toggleReaderMenu(){
        FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
        FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
        FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);
        FrameLayout imageLoaderContainer = (FrameLayout) findViewById(R.id.image_loader);
        FrameLayout progressBar = findViewById(R.id.progress_bar_loader);
        ImageView imageViewer = findViewById(R.id.image_viewer);
        progressBar.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);
        FrameLayout closeMenu = findViewById(R.id.close_menu);

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
            SystemUiHelper.showSystemUI(this);

            topMenu.setVisibility(View.VISIBLE);
            bottomMenu.setVisibility(View.VISIBLE);
            closeMenu.setVisibility(View.VISIBLE);

            topMenu.startAnimation(animTranslateIn);
            bottomMenu.startAnimation(animTranslateBottomIn);
        }else{
            SystemUiHelper.hideSystemUI(this);

            topMenu.startAnimation(animTranslateOut);
            bottomMenu.startAnimation(animTranslateBottomOut);

            closeMenu.setVisibility(View.GONE);
            topMenu.setVisibility(View.GONE);
            bottomMenu.setVisibility(View.GONE);

            imageLoaderContainer.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            imageViewer.setImageBitmap(null);
        }
    }

    public void loadImage(String imageSrc){
        FrameLayout imageLoaderContainer = (FrameLayout) findViewById(R.id.image_loader);
        imageLoaderContainer.setVisibility(View.VISIBLE);

        LoadImageTask loadImageTask = new LoadImageTask();
        loadImageTask.execute(imageSrc);
    }

    public void changeReaderSettings(float font_size, String font_name, String font_color, String background_color){
        userReaderPreferences.setUserReaderPreferences(font_size, font_name, font_color, background_color);

        if(normalViewController != null){
            normalViewController.applyNewUserReaderPreferences();
        }
    }

    public void getPreviousChapter(){
        Chapter previous = nrc.getPreviousChapter();

        if(previous == null || previous.getChapterIndex() == null){
            Toast.makeText(this, "Não tem capitulo anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        if(nrc.getCurrentPreviousChapter() == null || nrc.getCurrentPreviousChapter().getChapterIndex() == null){
            if(normalViewController.pageViewController != null){
                normalViewController.pageViewController.addInvalidPage(2);
                return;
            }
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        loadChapter(2);

        mAdapter.updateCurrentChapter();
    }

    public void getNextChapter() {
        int currentChapter = nrc.getCurrentChapter().getChapterIndex().getId();

        if(currentChapter != -1 && !chaptersReadied.contains(currentChapter)){
            chaptersReadied.add(currentChapter);
            DBController db = new DBController(this);
            db.setChapterAsReaded(currentChapter);
        }

        Chapter next = nrc.getNextChapter();

        if(next == null || next.getChapterIndex() == null){
            Toast.makeText(this, "Não tem capitulo posterior", Toast.LENGTH_SHORT).show();
            return;
        }

        if(nrc.getCurrentNextChapter() == null || nrc.getCurrentNextChapter().getChapterIndex() == null){
            if(normalViewController.pageViewController != null){
                normalViewController.pageViewController.addInvalidPage(1);
                return;
            }
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        loadChapter(1);

        mAdapter.updateCurrentChapter();
    }

    public void goToChapter(int chapter){
        FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);
        nrc.goToChapter(chapter);

        if(sideMenu.getVisibility() == View.VISIBLE){
            sideMenu.startAnimation(animTranslateSideOut);
            sideMenu.setVisibility(View.GONE);
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        loadChapter();

        mAdapter.updateCurrentChapter();
    }

    public void lastChapterFinished(){
        if(nrc.getCurrentChapter().exist() &&  !nrc.getCurrentNextChapter().exist()){
            int currentChapter = nrc.getCurrentChapter().getChapterIndex().getId();

            if(currentChapter != -1 && !chaptersReadied.contains(currentChapter)){
                chaptersReadied.add(currentChapter);
                DBController db = new DBController(this);
                db.setChapterAsReaded(currentChapter);
            }
        }
    }

    public void updateChapterList(String novelName, String novelSource){
        if(novelName.equals(this.novelName) && novelSource.equals(this.sourceName)){
            UpdateChaptersList updateChaptersList = new UpdateChaptersList();
            updateChaptersList.execute();

            checkForDownloaderServiceStarted = new Thread(new CheckForDownloadServiceRunningTask());
            checkForServiceStartedThread.start();
        }
    }

    public void setChapterAsDownloaded(int chapterId){
        if(!chaptersDownloaded.contains(chapterId)){
            chaptersDownloaded.add(chapterId);

            ChapterDownloaded(chapterId, false);
        }
    }

    public void ChapterDownloaded(int chapterId, boolean hadError){
        mAdapter.ChapterDownloaded(chapterId, hadError);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("readiedChapters", chaptersReadied);
        data.putExtra("downloadedChapters", chaptersDownloaded);
        data.putExtra("currentReaderViewType", readerViewType);
        setResult(RESULT_OK,data);

        checkForServiceStartedThread.interrupt();
        if(checkForDownloaderServiceStarted != null)
            checkForDownloaderServiceStarted.interrupt();

        finish();
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
                    Intent serviceIntent = new Intent(ctx, DownloaderService.class);
                    serviceIntent.putExtra("receiver2", (Parcelable) downloadReceiver);
                    ctx.startService(serviceIntent);
                }

                SystemClock.sleep(1000);
            }
        }
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {
            String filePath = novelName.replace(" ", "_") + "/" + strings[0];
            File mSaveBit = new File(ctx.getFilesDir(), filePath);;
            String imagePath = mSaveBit.getPath();

            return BitmapFactory.decodeFile(imagePath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

/*            FrameLayout progressBar = findViewById(R.id.progress_bar_loader);
            progressBar.setVisibility(View.GONE);*/

            ImageView imageViewer = findViewById(R.id.image_viewer);

            imageViewer.setImageBitmap(bitmap);
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
            loadCurrentChapter();
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

