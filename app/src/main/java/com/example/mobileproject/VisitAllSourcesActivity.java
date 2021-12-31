package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;

import java.util.ArrayList;
import java.util.List;

public class VisitAllSourcesActivity  extends AppCompatActivity {

    Context ctx = this;
    private RecyclerView mRecyclerView;
    private SearchNovelGridAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_in_all_sources);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        String searchText = intent.getStringExtra("search_text");

        mRecyclerView = findViewById(R.id.novelsGrid);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(ctx);

        mAdapter = new SearchNovelGridAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);


        Button simpleButton1 = (Button) findViewById(R.id.return_activity);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        EditText editText = (EditText) findViewById(R.id.search_text);
        editText.setText(searchText);

        ImageButton simpleButton2 = (ImageButton) findViewById(R.id.search_button);
        simpleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter = new SearchNovelGridAdapter(VisitAllSourcesActivity.this);
                mRecyclerView.setAdapter(mAdapter);

                startSearch(editText.getText().toString());
            }
        });

        startSearch(searchText);
    }

    private void startSearch(String searchText){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(500);
                mAdapter.Search(searchText);
            }
        }).start();
    }
}