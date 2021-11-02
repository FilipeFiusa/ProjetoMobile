package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;

import org.w3c.dom.Text;

public class VisitSourceWebViewActivity extends AppCompatActivity {
    Context ctx = this;
    private String currentSourceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_source_webview);

        Intent i = getIntent();
        currentSourceName = (String) i.getStringExtra("SourceName");//NovelLink
        String novelLink = (String) i.getStringExtra("NovelLink");

        TextView textView = (TextView) findViewById(R.id.source_name);
        textView.setText(currentSourceName);

        Button button = (Button) findViewById(R.id.return_activity_w);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Parser p = (Parser) ParserFactory.getParserInstance(currentSourceName, ctx);

        WebView wv = (WebView) findViewById(R.id.web_view);

        WebSettings ws = wv.getSettings();

        ws.setJavaScriptEnabled(true);

        wv.loadUrl(p.getURL_BASE() + novelLink);

    }
}
