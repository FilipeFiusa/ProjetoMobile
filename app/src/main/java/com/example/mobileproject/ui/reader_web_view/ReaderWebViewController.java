package com.example.mobileproject.ui.reader_web_view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;

import java.util.Objects;

public class ReaderWebViewController  {
    private CoordinatorLayout layout;
    private WebView webView;
    private ReaderActivity ctx;
    private ChapterIndex currentChapter;
    private String sourceName;
    private ChapterContent currentChapterContent;

    private int isLinkEnabled = 0;
    private String baseUrl = "";

    public ReaderWebViewController(CoordinatorLayout layout, ReaderActivity ctx, ChapterIndex currentChapter, String sourceName, ChapterContent currentChapterContent) {
        this.layout = layout;
        this.ctx = ctx;
        this.currentChapter = currentChapter;
        this.sourceName = sourceName;
        this.currentChapterContent = currentChapterContent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
           ctx.getWindow().getAttributes().layoutInDisplayCutoutMode = 0;
        }

        setUpMenu();
        setUpWebView();
    }

    private void setUpMenu(){
        SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);
        isLinkEnabled = preferences.getInt("isLinkEnabled", 0);;

        if(isLinkEnabled == 0){
            setUpPopUp(preferences.edit());
        }

        try{
            baseUrl = ((Parser) Objects.requireNonNull(ParserFactory.getParserInstance(sourceName, ctx))).getUrlBase();
        }catch (NullPointerException e){
            baseUrl = "";
            currentChapter.setChapterLink("");
        }


        FrameLayout hideMenu = (FrameLayout) layout.findViewById(R.id.hide_reader_menu);
        hideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.toggleReaderMenu();
                hideMenu.setVisibility(View.GONE);
            }
        });

        ImageButton returnButton = (ImageButton) layout.findViewById(R.id.reader_web_view_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.finish();
            }
        });

        Button previousButton = (Button) layout.findViewById(R.id.reader_web_view_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getPreviousChapter();
            }
        });

        Button menuButton = (Button) layout.findViewById(R.id.reader_web_view_menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.toggleReaderMenu();
                hideMenu.setVisibility(View.VISIBLE);
            }
        });

        Button nextButton = (Button) layout.findViewById(R.id.reader_web_view_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.getNextChapter();
            }
        });

        TextView currentLink = (TextView) layout.findViewById(R.id.current_link);
        currentLink.setText(new StringBuilder().append(baseUrl).append(currentChapter.getChapterLink().replace(baseUrl, "")).toString());
    }

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    private void  setUpWebView(){
        webView = (WebView) layout.findViewById(R.id.reader_web_view);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(isLinkEnabled == 1 || isLinkEnabled == 0){
                    return false;
                }else{
                    return true;
                }
            }
        });

        if(currentChapterContent == null || currentChapter == null){
            return;
        }

        webView.loadDataWithBaseURL(baseUrl + currentChapter.getChapterLink(),
                currentChapterContent.getRawChapter(),
                "text/html",
                "UTF-8",
                null
        );
    }

    private void setUpPopUp(SharedPreferences.Editor editor){
        FrameLayout popUp = layout.findViewById(R.id.reader_web_view_pop_up);
        popUp.setVisibility(View.VISIBLE);

        Button refuse = (Button) popUp.findViewById(R.id.refuse_button);
        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLinkEnabled = 1;
                editor.putInt("isLinkEnabled", isLinkEnabled);
                editor.apply();
                popUp.setVisibility(View.GONE);
            }
        });

        Button disable = (Button) popUp.findViewById(R.id.disable_button);
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLinkEnabled = 2;
                editor.putInt("isLinkEnabled", isLinkEnabled);
                editor.apply();
                popUp.setVisibility(View.GONE);
            }
        });
    }

    public void goToAnotherChapter(ChapterIndex chapter, ChapterContent content){
        this.currentChapter = chapter;

        //webView.loadUrl(baseUrl + currentChapter.getChapterLink());
        webView.loadDataWithBaseURL(baseUrl + currentChapter.getChapterLink(),
                content.getRawChapter(),
                "text/html",
                "UTF-8",
                null
        );

        TextView currentLink = (TextView) layout.findViewById(R.id.current_link);
        currentLink.setText(new StringBuilder().append(baseUrl).append(currentChapter.getChapterLink().replace(baseUrl, "")).toString());
    }

    public void openLink(String link){
        webView.loadUrl(link);
    }

}
