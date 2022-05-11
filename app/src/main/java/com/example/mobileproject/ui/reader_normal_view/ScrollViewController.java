package com.example.mobileproject.ui.reader_normal_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.UserReaderPreferences;
import com.example.mobileproject.util.TagReplacerHelper;

public class ScrollViewController {
    private FrameLayout container;
    private ReaderActivity ctx;
    private NovelReaderController nrc;

    private TextView titleTextView;
    private TextView chapterTextView;
    private ScrollView scrollView;
    private LinearLayout background;

    final private UserReaderPreferences userReaderPreferences;

    public ScrollViewController(FrameLayout container, ReaderActivity ctx, NovelReaderController nrc,
                                UserReaderPreferences userReaderPreferences) {
        this.container = container;
        this.ctx = ctx;
        this.nrc = nrc;

        this.userReaderPreferences = userReaderPreferences;

        setUpLayout();
    }

    public void setUpLayout(){
        titleTextView = (TextView) container.findViewById(R.id.chapter_name);
        chapterTextView = (TextView) container.findViewById(R.id.chapter_content);
        scrollView = (ScrollView) container.findViewById(R.id.reader_scroll_view);
        background = ctx.findViewById(R.id.reader_container);

        chapterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.toggleReaderMenu();
            }
        });

        titleTextView.setOnClickListener(new View.OnClickListener() {
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
            }
        });

        Button nextButton = (Button) container.findViewById(R.id.reader_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getNextChapter();
            }
        });

        userReaderPreferences.applyPreferences(background, titleTextView, chapterTextView);
    }

    public void setChapterContent(ChapterContent chapterContent){
        String firstCapitalizedLetter = chapterContent.getChapterName().substring(0,1).toUpperCase();
        titleTextView.setText(String.format("%s%s", firstCapitalizedLetter, chapterContent.getChapterName().substring(1)));
        chapterTextView.setText(TagReplacerHelper.replaceAll(ctx, chapterContent.getChapterContent()));
        chapterTextView.setMovementMethod(LinkMovementMethod.getInstance());

        scrollView.fullScroll(ScrollView.FOCUS_UP);
        scrollView.pageScroll(ScrollView.FOCUS_UP);
        scrollView.smoothScrollTo(0,0);
    }

    public void applyNewUserReaderPreferences(){
        userReaderPreferences.applyPreferences(background, titleTextView, chapterTextView);
    }
}
