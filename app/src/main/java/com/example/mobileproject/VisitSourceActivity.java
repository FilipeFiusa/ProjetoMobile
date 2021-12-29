package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.ui.source_top_fragments.TopFragment1;

import java.util.ArrayList;

public class VisitSourceActivity extends AppCompatActivity {
    Context ctx = this;
    private String currentSourceName;

    private NovelsFromThisSource searchWorker = null;
    //private LoadImagesTask loadImagesWorker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_source);

        Intent i = getIntent();
        currentSourceName = (String) i.getStringExtra("SourceName");

        loadFragment((Fragment) new TopFragment1(currentSourceName));

        NovelsFromThisSource novelsFromThisSource = new NovelsFromThisSource();
        novelsFromThisSource.execute();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.visit_source_top_fragments, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    public class NovelsFromThisSource extends AsyncTask<Void, Void, ArrayList<NovelDetailsMinimum>> {
        private LinearLayout isLoading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = (LinearLayout) findViewById(R.id.isLoading);
        }

        @Override
        protected ArrayList<NovelDetailsMinimum> doInBackground(Void... voids) {
            ArrayList<NovelDetailsMinimum> novelDetailsArr;

            ParserInterface parser = ParserFactory.getParserInstance(currentSourceName, ctx);

            if(parser == null){
                System.out.println("Null");
                return null;
            }

            novelDetailsArr = parser.getHotNovels();

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<NovelDetailsMinimum> novelDetailsArr) {
            super.onPostExecute(novelDetailsArr);
            isLoading.setVisibility(View.INVISIBLE);
            isLoading.removeAllViews();

            if(novelDetailsArr == null){
                return;
            }

            GridView gv = (GridView) findViewById(R.id.novelsGrid);
            gv.setAdapter(new NovelsGridAdaptor((Context) VisitSourceActivity.this, novelDetailsArr, currentSourceName));
        }
    }
}