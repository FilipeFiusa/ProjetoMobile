package com.example.mobileproject.ui.reader_normal_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.util.FontFactory;

public class ReaderNormalView {

    private FrameLayout layout;
    private ReaderActivity ctx;

    private TextView chapterName;
    private TextView chapterContentView;

    private ScrollView scrollView;

    public ReaderNormalView(FrameLayout layout, ReaderActivity ctx, ChapterContent currentChapter) {
        this.layout = layout;
        this.ctx = ctx;

        chapterName = (TextView) layout.findViewById(R.id.chapter_name);
        chapterContentView = (TextView) layout.findViewById(R.id.chapter_content);
        scrollView = (ScrollView) layout.findViewById(R.id.reader_scroll_view);

        setUpLayout();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ctx.getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if(currentChapter != null){
            setChapterContent(currentChapter);
        }
    }

    private void setUpLayout(){
        TextView textView = (TextView) layout.findViewById(R.id.chapter_content);
        textView.setOnClickListener(new View.OnClickListener() {
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

        Button previousButton = (Button) layout.findViewById(R.id.reader_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getPreviousChapter();
            }
        });

        Button nextButton = (Button) layout.findViewById(R.id.reader_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getNextChapter();
            }
        });

        setReaderUserPreferences();
    }

    private void setReaderUserPreferences(){
        SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

        LinearLayout container = ctx.findViewById(R.id.reader_container);
        container.setBackgroundColor(Color.parseColor(preferences.getString("background_color", "#1F1B1B")));

        TextView chapterContentView = (TextView) layout.findViewById(R.id.chapter_content);
        chapterContentView.setTextSize(preferences.getFloat("font_size", 18));
        chapterContentView.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
        chapterContentView.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));
        TextView chapterTitleView = (TextView) layout.findViewById(R.id.chapter_name);
        chapterTitleView.setTextSize(preferences.getFloat("font_size", 20) + 15);
        chapterTitleView.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
        chapterTitleView.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));
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

    public void setChapterContent(ChapterContent chapterContent){
        chapterName.setText(chapterContent.getChapterName());
        chapterContentView.setText(chapterContent.getChapterContent());

        scrollView.fullScroll(ScrollView.FOCUS_UP);
        scrollView.pageScroll(ScrollView.FOCUS_UP);
        scrollView.smoothScrollTo(0,0);
    }
}
