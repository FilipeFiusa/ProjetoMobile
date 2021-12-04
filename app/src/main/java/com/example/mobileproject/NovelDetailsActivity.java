package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.util.ArrayList;
import java.util.Objects;

public class NovelDetailsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DownloadReceiver downloadReceiver;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    Context ctx = this;

    NovelDetails currentNovel = null;

    boolean isTextViewClicked = false;
    boolean isFavorite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.novel_details);


        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                UpdateChapterList u = new UpdateChapterList();
                u.execute(currentNovel.getNovelLink(), currentNovel.getSource());
            }
        });

        mRecyclerView = findViewById(R.id.novel_details_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(ctx);
        mAdapter = new ChaptersAdapter(new ArrayList<>(), this);
        downloadReceiver = new DownloadReceiver(new Handler(), mAdapter);
        mAdapter.setDownloadReceiver(downloadReceiver);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Button simpleButton1 = (Button) findViewById(R.id.font_button_id);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button simpleButton3 = (Button) findViewById(R.id.downloadAll);
        simpleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //downloadAll();
            }
        });

        Intent i = getIntent();
        String novel_name = (String) i.getStringExtra("NovelDetails_name");
        String novel_source = (String) i.getStringExtra("NovelDetails_source");

        if(novel_name != null && novel_source != null){
            getNovelDetailsFromDB content = new getNovelDetailsFromDB();
            content.execute(novel_name, novel_source);
        }else{
            String novelLink = i.getStringExtra("novelLink");
            String novelName = i.getStringExtra("novelName");
            String novelSource = i.getStringExtra("novelSource");
            getNovelDetails content = new getNovelDetails();
            content.execute(novelLink, novelName, novelSource);
        }

        ImageButton imageButton = findViewById(R.id.shareButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentNovel == null){
                    return;
                }

                System.out.println("Apertou");
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                String shareBody = "Resumo: ";
                StringBuilder stringBuilder = new StringBuilder().append("Resumo: ")
                        .append(currentNovel.getNovelDescription()).append("\n\n")
                        .append("Acesse já esse novel pelo link: ")
                        .append( ( (Parser) Objects.requireNonNull(ParserFactory.getParserInstance(novel_source, NovelDetailsActivity.this))).getURL_BASE())
                        .append("/").append(currentNovel.getNovelLink());

                String shareTitle = "Leia a Novel " + currentNovel.getNovelName();
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, stringBuilder.toString());
                startActivity(Intent.createChooser(intent, "Share via"));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            ArrayList<Integer> readiedChapters = data.getIntegerArrayListExtra("readiedChapters");

            for(int id : readiedChapters){
                Log.i("id lidos", String.valueOf(id));
            }

            mAdapter.putChapterAsReadied(readiedChapters);

        }

    }

    private void downloadAll(){
        if(currentNovel == null || currentNovel.getChapterIndexes() == null){
            return;
        }

        Intent serviceIntent = new Intent(this, DownloaderService.class);
        serviceIntent.putExtra("NovelName", currentNovel.getNovelName());
        serviceIntent.putExtra("Source", currentNovel.getSource());
        serviceIntent.putExtra("receiver", (Parcelable) downloadReceiver);
        startService(serviceIntent);

        Toast.makeText(this, "Downloading All", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        setResult(RESULT_OK,data);

        finish();
    }

    private class getNovelDetails extends AsyncTask<String, NovelDetails, ArrayList<ChapterIndex>> {

        private String novelName, novelSource;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(String... novelDetails) {
            NovelDetails n = null;
            DBController db = new DBController(ctx);

            if(novelDetails.length > 1){
                n = db.getNovel(novelDetails[1], novelDetails[2]);
            }

            if(n != null){
                novelName = novelDetails[1];
                novelSource = novelDetails[2];

                return null;
            }

            ParserInterface parser = ParserFactory.getParserInstance(novelDetails[2], ctx);

            if (parser == null) {
                return null;
            }

            n = parser.getNovelDetails(novelDetails[0]);

            long id = db.insertNovel(
                    n.getNovelName(),
                    n.getNovelAuthor(),
                    n.getNovelDescription(),
                    n.getSource(),
                    n.getNovelImage(),
                    n.getNovelLink()
            );

            n.setDb_id((int) id);

            publishProgress(n);

            ArrayList<ChapterIndex> c = parser.getAllChaptersIndex(novelDetails[0]);

            for(ChapterIndex _c : c){
                db.insertChapters(n.getNovelName(), n.getSource(), _c);
            }

            return c;
        }

        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            if(currentNovel != null && currentNovel.equals(novelDetails[0])){
                return;
            }
            mAdapter.addNovelDetails(novelDetails[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndexes) {
            super.onPostExecute(chapterIndexes);

            if(chapterIndexes == null && novelName == null){
                mSwipeRefreshLayout.setRefreshing(false);
                return;
            }

            if(chapterIndexes == null){
                new getNovelDetailsFromDB().execute(novelName, novelSource);
                return;
            }

            mAdapter.addChapterIndexes(chapterIndexes);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateChapterList extends AsyncTask<String, Void, ArrayList<ChapterIndex>> {

        private String novelName, novelSource;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(String... novelLink) {
            ParserInterface parser = ParserFactory.getParserInstance(novelLink[1], ctx);
            if(parser == null){
                return null;
            }

            ArrayList<ChapterIndex> c = parser.getAllChaptersIndex(novelLink[0]);

            return c;
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndexes) {
            super.onPostExecute(chapterIndexes);

            mAdapter.addNewChapterIndexes(chapterIndexes);

            //mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class getNovelDetailsFromDB extends AsyncTask<String, NovelDetails, ArrayList<ChapterIndex>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(String... novelDetails) {
            DBController db = new DBController(NovelDetailsActivity.this);
            if(novelDetails[0] == null || novelDetails[1] == null){
                return null;
            }
            NovelDetails novel = db.getNovel(novelDetails[0], novelDetails[1]);
            if(novel == null){
                return null;
            }

            publishProgress(novel);

            ArrayList<ChapterIndex> c = db.getChaptersFromANovel(novel.getNovelName(), novel.getSource());

            return c;
        }


        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            currentNovel = novelDetails[0];
            mAdapter.addNovelDetails(novelDetails[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndexes) {
            super.onPostExecute(chapterIndexes);

            if(chapterIndexes == null){
                return;
            }

            mAdapter.addChapterIndexes(chapterIndexes);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


}