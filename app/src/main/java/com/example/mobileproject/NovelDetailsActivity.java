package com.example.mobileproject;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.DownloaderClass;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.ui.downloads.DownloadAdapter;

import java.nio.channels.Channel;
import java.util.ArrayList;

public class NovelDetailsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DownloadReceiver downloadReceiver;

    Context ctx = this;

    NovelDetails currentNovel = null;
    AddNovelOnFavorite addNovelOnFavorite = new AddNovelOnFavorite();

    boolean isTextViewClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.novel_details);

        mRecyclerView = findViewById(R.id.novel_details_recycle_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        //mRecyclerView.setItemAnimator(null);

        mLayoutManager = new LinearLayoutManager(ctx);
        mAdapter = new ChaptersAdapter(new ArrayList<>(), ctx);
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
                downloadAll();
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
            getNovelDetails content = new getNovelDetails();
            content.execute(novelLink);
        }

    }

    private void downloadAll(){
        if(currentNovel == null || currentNovel.getChapterIndexes() == null){
            return;
        }

        Intent serviceIntent = new Intent(this, DownloaderService.class);
        serviceIntent.putExtra("NovelName", currentNovel.getNovelName());
        serviceIntent.putExtra("Source", currentNovel.getSource());
        serviceIntent.putExtra("receiver", downloadReceiver);
        startService(serviceIntent);

        Toast.makeText(this, "Downloading All", Toast.LENGTH_SHORT).show();
    }


    private class getNovelDetails extends AsyncTask<String, NovelDetails, ArrayList<ChapterIndex>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(String... novelLink) {
            NovelDetails novelDetails;

            Parser parser = new NovelFullParser();
            novelDetails = parser.getNovelDetails(novelLink[0]);

            publishProgress(novelDetails);

            ArrayList<ChapterIndex> c = parser.getAllChaptersIndex(novelLink[0]);

            return c;
        }

        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            mAdapter.addNovelDetails(novelDetails[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndexes) {
            super.onPostExecute(chapterIndexes);

            mAdapter.addChapterIndexes(chapterIndexes);
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
            NovelDetails novel = db.getNovel(novelDetails[0], novelDetails[1]);
            novel.setIsFavorite("yes");

            publishProgress(novel);

            ArrayList<ChapterIndex> c = db.getChaptersFromANovel(novel.getNovelName(), "NovelFull");

            return c;
        }


        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            mAdapter.addNovelDetails(novelDetails[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndexes) {
            super.onPostExecute(chapterIndexes);

            mAdapter.addChapterIndexes(chapterIndexes);
        }
    }

    private class AddNovelOnFavorite extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DBController db = new DBController(getApplicationContext());
            db.insertNovel(currentNovel.getNovelName(), currentNovel.getNovelAuthor(), currentNovel.getNovelDescription(), "NovelFull",  currentNovel.getNovelImage());
            ArrayList<ChapterIndex> chapterIndices = currentNovel.getChapterIndexes();
            for(ChapterIndex c : chapterIndices){
                db.insertChapters(currentNovel.getNovelName(), "NovelFull", c);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

    private class PutChapterOnDownloadList extends AsyncTask<Integer, Void, Boolean> {

        ImageButton imageButton;

        public PutChapterOnDownloadList(ImageButton imageButton){
            this.imageButton = imageButton;
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            DBController db = new DBController(getApplicationContext());

            return db.putChapterOnDownload(currentNovel.getNovelName(),
                    currentNovel.getSource(), integers[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                Toast.makeText(NovelDetailsActivity.this, "Baixando", Toast.LENGTH_SHORT).show();
                imageButton.setImageResource(R.drawable.ic_outline_cancel_40);
            }
        }
    }
}