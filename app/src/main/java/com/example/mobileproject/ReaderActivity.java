package com.example.mobileproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;


public class ReaderActivity extends AppCompatActivity {
    private NovelReaderController nrc;
    private GetChapterContent getChapterContent;
    Button previousButton;
    Button nextButton;

    private RecyclerView mRecyclerView;
    private ReaderChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    Animation animTranslateIn;
    Animation animTranslateOut;
    Animation animTranslateBottomIn;
    Animation animTranslateBottomOut;
    Animation animTranslateSideIn;
    Animation animTranslateSideOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        animTranslateIn = AnimationUtils.loadAnimation(this, R.anim.translate_in);
        animTranslateOut = AnimationUtils.loadAnimation(this, R.anim.translate_out);
        animTranslateBottomIn = AnimationUtils.loadAnimation(this, R.anim.translate_botton_in);
        animTranslateBottomOut = AnimationUtils.loadAnimation(this, R.anim.translate_bottom_out);
        animTranslateSideIn = AnimationUtils.loadAnimation(this, R.anim.translate_side_in);
        animTranslateSideOut = AnimationUtils.loadAnimation(this, R.anim.translate_side_out);


        Intent i = getIntent();
        String chapterLink = i.getStringExtra("chapterLink");
        nrc = (NovelReaderController) i.getSerializableExtra("NovelReaderController");
        Log.i("--", nrc.setStartedChapter(chapterLink).getChapterLink());

        mRecyclerView = findViewById(R.id.reader_menu_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ReaderChaptersAdapter(nrc, chapterLink, this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        previousButton = (Button) findViewById(R.id.reader_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreviousChapter();
            }
        });

        nextButton = (Button) findViewById(R.id.reader_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNextChapter();
            }
        });

        setUpMenu();
        /*
        LinearLayout container = (LinearLayout) findViewById(R.id.chapter_container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                topMenu.startAnimation(animTranslateIn);
                /*
                if (topMenu.getVisibility() == View.GONE){
                    topMenu.setVisibility(View.VISIBLE);
                    topMenu.startAnimation(animTranslateIn);

                }else{
                    topMenu.startAnimation(animTranslateOut);

                    topMenu.setVisibility(View.GONE);
                }


            }
        });
        */

        TextView textView = (TextView) findViewById(R.id.chapter_content);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
                FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

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
        });

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(nrc.getCurrentChapter());
    }

    private void setUpMenu(){
        Intent i = getIntent();
        String novelName = i.getStringExtra("novelName");

        ImageButton returnButton = (ImageButton) findViewById(R.id.reader_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        TextView textProgress = (TextView) findViewById(R.id.reader_seekbar_progress);
        textProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        SeekBar seekBar = (SeekBar) findViewById(R.id.novel_progress);
        seekBar.setProgress(0);
        seekBar.setMax(nrc.getSize() - 1);
        seekBar.setProgress(nrc.getPosition());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textProgress.setText(new StringBuilder().append(i + 1).append("/").append(nrc.getSize()).toString());
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
    }

    private void getPreviousChapter(){
        if(getChapterContent.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }

        ChapterIndex previous = nrc.getPreviousChapter();

        if(previous == null){
            Toast.makeText(this, "Não tem capitulo anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(previous);
    }

    private void getNextChapter() {
        if(getChapterContent.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }

        ChapterIndex next = nrc.getNextChapter();

        if(next == null){
            Toast.makeText(this, "Não tem capitulo anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(next);
    }

    public void goTo(ChapterIndex c){
        FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

        if(sideMenu.getVisibility() == View.VISIBLE){
            sideMenu.startAnimation(animTranslateSideOut);
            sideMenu.setVisibility(View.GONE);
        }

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(c);
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY

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

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private class GetChapterContent extends AsyncTask<ChapterIndex, Void, ChapterContent> {
        private TextView chapterName;
        private TextView chapterNameBottom;
        private TextView chapterContentView;

        private ScrollView scrollView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            chapterName = (TextView) findViewById(R.id.chapter_name);
            chapterNameBottom = (TextView) findViewById(R.id.chapter_name_bottom);
            chapterContentView = (TextView) findViewById(R.id.chapter_content);

            scrollView = (ScrollView) findViewById(R.id.reader_scroll_view);
        }

        @Override
        protected ChapterContent doInBackground(ChapterIndex... chapter) {
            ChapterContent chapterContent;

            if(chapter[0].getDownloaded().equals("yes") || chapter[0].getDownloaded().equals("downloading")){
                DBController db = new DBController(ReaderActivity.this);
                chapterContent = db.getChapter(chapter[0].getId());

                if(!chapterContent.getChapterContent().isEmpty()){
                    return chapterContent;
                }
            }

            Parser parser = new NovelFullParser();
            chapterContent = parser.getChapterContent(chapter[0].getChapterLink());

            return chapterContent;
        }

        @Override
        protected void onPostExecute(ChapterContent chapterContent) {
            super.onPostExecute(chapterContent);

            if(chapterContent == null || chapterContent.getChapterContent().isEmpty()){
                Toast.makeText(ReaderActivity.this, "Falha ao acessar", Toast.LENGTH_SHORT).show();
                return;
            }

            chapterName.setText(chapterContent.getChapterName());
            chapterNameBottom.setText(chapterContent.getChapterName());
            chapterContentView.setText(chapterContent.getChapterContent());

            scrollView.fullScroll(ScrollView.FOCUS_UP);
            scrollView.pageScroll(ScrollView.FOCUS_UP);
            scrollView.smoothScrollTo(0,0);
        }
    }

}

