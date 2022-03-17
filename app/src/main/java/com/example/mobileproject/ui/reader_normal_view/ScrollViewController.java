package com.example.mobileproject.ui.reader_normal_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.util.FontFactory;

public class ScrollViewController {
    private FrameLayout container;
    private ReaderActivity ctx;
    private NovelReaderController nrc;

    private TextView chapterName;
    private TextView chapterContentView;
    private ScrollView scrollView;


    public ScrollViewController(FrameLayout container, ReaderActivity ctx, NovelReaderController nrc) {
        this.container = container;
        this.ctx = ctx;
        this.nrc = nrc;

        setUpLayout();
    }

    public void setUpLayout(){
        chapterName = (TextView) container.findViewById(R.id.chapter_name);
        chapterContentView = (TextView) container.findViewById(R.id.chapter_content);
        scrollView = (ScrollView) container.findViewById(R.id.reader_scroll_view);

        chapterContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.toggleReaderMenu();
            }
        });

        chapterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.toggleReaderMenu();
            }
        });

        Button previousButton = (Button) container.findViewById(R.id.reader_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getPreviousChapter();
/*

                ChapterContent currentChapter = nrc.getCurrentChapter().getChapterContent();
                setChapterContent(currentChapter);*/
            }
        });

        Button nextButton = (Button) container.findViewById(R.id.reader_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getNextChapter();


/*                ChapterContent currentChapter = nrc.getCurrentChapter().getChapterContent();

                chapterName.setText(currentChapter.getChapterName());
                chapterContentView.setText(currentChapter.getChapterContent());

                scrollView.fullScroll(ScrollView.FOCUS_UP);
                scrollView.pageScroll(ScrollView.FOCUS_UP);
                scrollView.smoothScrollTo(0,0);*/
            }
        });

        setReaderUserPreferences();
    }

    private void setReaderUserPreferences(){
        SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

        LinearLayout background = ctx.findViewById(R.id.reader_container);
        background.setBackgroundColor(Color.parseColor(preferences.getString("background_color", "#1F1B1B")));

        TextView chapterContentView = (TextView) container.findViewById(R.id.chapter_content);
        chapterContentView.setTextSize(preferences.getFloat("font_size", 18));
        chapterContentView.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
        chapterContentView.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));
        TextView chapterTitleView = (TextView) container.findViewById(R.id.chapter_name);
        chapterTitleView.setTextSize(preferences.getFloat("font_size", 20) + 15);
        chapterTitleView.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
        chapterTitleView.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));
    }

    public void setChapterContent(ChapterContent chapterContent){
        chapterName.setText(chapterContent.getChapterName());
        chapterContentView.setText(chapterContent.getChapterContent());

        scrollView.fullScroll(ScrollView.FOCUS_UP);
        scrollView.pageScroll(ScrollView.FOCUS_UP);
        scrollView.smoothScrollTo(0,0);
    }
}
