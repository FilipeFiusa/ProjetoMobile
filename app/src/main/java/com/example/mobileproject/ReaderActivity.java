package com.example.mobileproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;


public class ReaderActivity extends AppCompatActivity {
    private ScrollView scrollView;
    private NovelReaderController nrc;
    GetChapterContent getChapterContent;
    Button previousButton;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        Intent i = getIntent();
        String chapterLink = i.getStringExtra("chapterLink");
        nrc = (NovelReaderController) i.getSerializableExtra("NovelReaderController");
        Log.i("--", nrc.getCurrentPosition(chapterLink).getChapterLink());

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

        LinearLayout container = (LinearLayout) findViewById(R.id.chapter_container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                if (topMenu.getVisibility() == View.GONE){
                    topMenu.setVisibility(View.VISIBLE);
                }else{
                    topMenu.setVisibility(View.GONE);
                }
            }
        });
        TextView textView = (TextView) findViewById(R.id.chapter_content);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                if (topMenu.getVisibility() == View.GONE){
                    topMenu.setVisibility(View.VISIBLE);
                }else{
                    topMenu.setVisibility(View.GONE);
                }
            }
        });

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(chapterLink);
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
        getChapterContent.execute(previous.getChapterLink());
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
        getChapterContent.execute(next.getChapterLink());
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

    private class GetChapterContent extends AsyncTask<String, Void, ChapterContent> {
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
        protected ChapterContent doInBackground(String... chapterLink) {
            ChapterContent chapterContent;

            Parser parser = new NovelFullParser();
            chapterContent = parser.getChapterContent(chapterLink[0]);

            return chapterContent;
        }

        @Override
        protected void onPostExecute(ChapterContent chapterContent) {
            super.onPostExecute(chapterContent);

            chapterName.setText(chapterContent.getChapterName());
            chapterNameBottom.setText(chapterContent.getChapterName());
            chapterContentView.setText(chapterContent.getChapterContent());

            scrollView.fullScroll(ScrollView.FOCUS_UP);
            scrollView.smoothScrollTo(0,0);
        }
    }

}

