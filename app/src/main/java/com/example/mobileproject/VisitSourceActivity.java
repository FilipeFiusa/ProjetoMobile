package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.ui.library.LibraryFragment;
import com.example.mobileproject.ui.library.NovelsAdapter;
import com.example.mobileproject.ui.library.NovelsLayoutDecoration;
import com.example.mobileproject.ui.source_top_fragments.TopFragment1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class VisitSourceActivity extends AppCompatActivity {
    AppCompatActivity ctx = this;
    private String currentSourceName;

    private NovelsFromThisSource searchSourceWorker = null;
    private SearchNovelsFromThisSource searchWorker = null;
    private LoadImagesTask loadImagesWorker = null;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NovelsGridAdaptor mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_source);

        Intent i = getIntent();
        currentSourceName = (String) i.getStringExtra("SourceName");

        loadFragment((Fragment) new TopFragment1(currentSourceName, this));

        mSwipeRefreshLayout = findViewById(R.id.visit_source_swipe_refresh);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView = findViewById(R.id.visit_source_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(ctx, 2);
        mAdapter = new NovelsGridAdaptor(new ArrayList<>(), ctx, currentSourceName);
        mRecyclerView.addItemDecoration(new NovelsLayoutDecoration(10, ctx));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        searchSourceWorker = new NovelsFromThisSource();
        searchSourceWorker.execute();
    }

    public void SearchNovel(String searchValue){
        if( (searchSourceWorker != null && searchSourceWorker.getStatus() == AsyncTask.Status.RUNNING)
                || (searchWorker != null && searchWorker.getStatus() == AsyncTask.Status.RUNNING )){
            return;
        }

        if(loadImagesWorker.getStatus() == AsyncTask.Status.RUNNING){
            loadImagesWorker.cancel(true);
        }

        mSwipeRefreshLayout.setRefreshing(true);

        searchWorker = new SearchNovelsFromThisSource();
        searchWorker.execute(searchValue);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.visit_source_top_fragments, fragment);
        fragmentTransaction.commit();
    }

    public class NovelsFromThisSource extends AsyncTask<Void, Void, ArrayList<NovelDetailsMinimum>> {
        private LinearLayout isLoading;

        @Override
        protected ArrayList<NovelDetailsMinimum> doInBackground(Void... voids) {
            ArrayList<NovelDetailsMinimum> novelDetailsArr;

            ParserInterface parser = ParserFactory.getParserInstance(currentSourceName, ctx);

            if(parser == null){
                return null;
            }

            novelDetailsArr = parser.getHotNovels();

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<NovelDetailsMinimum> novelDetailsArr) {
            super.onPostExecute(novelDetailsArr);
            mSwipeRefreshLayout.setRefreshing(false);

            if(novelDetailsArr == null){
                return;
            }

            mAdapter.updateNovelsList(novelDetailsArr);

            loadImagesWorker = new LoadImagesTask();
            loadImagesWorker.execute(novelDetailsArr);
        }
    }

    private class SearchNovelsFromThisSource extends AsyncTask<String, Void, ArrayList<NovelDetailsMinimum>> {

        @Override
        protected ArrayList<NovelDetailsMinimum> doInBackground(String... params) {
            ArrayList<NovelDetailsMinimum> novelDetailsArr;

            ParserInterface parser = ParserFactory.getParserInstance(currentSourceName, ctx);

            if(parser == null){
                return null;
            }

            novelDetailsArr = parser.searchNovels(params[0]);

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<NovelDetailsMinimum> novelDetailsArr) {
            super.onPostExecute(novelDetailsArr);
            mSwipeRefreshLayout.setRefreshing(false);

            if(novelDetailsArr == null){
                return;
            }

            mAdapter.updateNovelsList(novelDetailsArr);

            loadImagesWorker = new LoadImagesTask();
            loadImagesWorker.execute(novelDetailsArr);
        }
    }

    public class LoadImagesTask extends AsyncTask<ArrayList<NovelDetailsMinimum>, NovelDetailsMinimum, Void>{

        @Override
        protected Void doInBackground(ArrayList<NovelDetailsMinimum>... arrayLists) {
            ArrayList<NovelDetailsMinimum> novels = arrayLists[0];

            for (int i = 0; i < novels.size(); i++) {
                NovelDetailsMinimum current = novels.get(i);

                InputStream input = null;
                try {
                    input = new URL(current.getNovelImageSrc()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    current.setNovelImage(bitmap);

                    publishProgress(current);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(NovelDetailsMinimum... values) {
            super.onProgressUpdate(values);

            mAdapter.updateSpecificItem(values[0]);
        }
    }
}