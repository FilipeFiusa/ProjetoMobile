package com.example.mobileproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
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

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloaderClass;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.nio.channels.Channel;
import java.util.ArrayList;

public class NovelDetailsActivity extends AppCompatActivity {
    Context ctx = this;

    NovelDetails currentNovel = null;
    AddNovelOnFavorite addNovelOnFavorite = new AddNovelOnFavorite();

    boolean isTextViewClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.novel_details);

        Button simpleButton1 = (Button) findViewById(R.id.font_button_id);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button simpleButton2 = (Button) findViewById(R.id.add_favorite);
        simpleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFavorite();
            }
        });

        Button simpleButton3 = (Button) findViewById(R.id.downloadAll);
        simpleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadAll();
            }
        });

        TextView t1 = (TextView) findViewById(R.id.novel_description);
        t1.setMaxLines(3);
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    t1.setMaxLines(3);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    t1.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
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
        startService(serviceIntent);


        Toast.makeText(this, "Downloading All", Toast.LENGTH_SHORT).show();
    }

    private void setFavorite(){
        if(currentNovel == null || addNovelOnFavorite.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }
        Button simpleButton2 = (Button) findViewById(R.id.add_favorite);
        simpleButton2.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.ic_baseline_favorite_64,
                0,
                0);

        simpleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unsetFavorite();
            }
        });


        addNovelOnFavorite = new AddNovelOnFavorite();
        addNovelOnFavorite.execute();
    }

    private void unsetFavorite() {
        return;
    }

    private class getNovelDetails extends AsyncTask<String, NovelDetails, NovelDetails> {
        private TextView title;
        private TextView author;
        private TextView description;
        private ImageView image;
        private LinearLayout chapterContainer;
        private TextView chapterQuantity;
        private NotificationCompat.Builder notificationProgress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            title = (TextView) findViewById(R.id.novel_name);
            author = (TextView) findViewById(R.id.novel_author);
            description = (TextView) findViewById(R.id.novel_description);
            image = (ImageView) findViewById(R.id.novel_image);
            chapterContainer = (LinearLayout) findViewById(R.id.novel_container);
            chapterQuantity = (TextView) findViewById(R.id.chapter_quantity);
        }

        @Override
        protected NovelDetails doInBackground(String... novelLink) {
            NovelDetails novelDetails;

            Parser parser = new NovelFullParser();
            novelDetails = parser.getNovelDetails(novelLink[0]);

            publishProgress(novelDetails);

            novelDetails.setChapterIndexes(parser.getAllChaptersIndex(novelLink[0]));

            return novelDetails;
        }

        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            title.setText(novelDetails[0].getNovelName());
            description.setText(novelDetails[0].getNovelDescription());
            author.setText(novelDetails[0].getNovelAuthor());
            image.setImageBitmap(novelDetails[0].getNovelImage());

        }

        @Override
        protected void onPostExecute(NovelDetails novelDetails) {
            super.onPostExecute(novelDetails);
            ArrayList<ChapterIndex> chapterIndices;

            if(novelDetails == null){
                return;
            }

            title.setText(novelDetails.getNovelName());
            description.setText(novelDetails.getNovelDescription());
            author.setText(novelDetails.getNovelAuthor());
            image.setImageBitmap(novelDetails.getNovelImage());

            chapterIndices = novelDetails.getChapterIndexes();

            chapterQuantity.setText(new StringBuilder().append(chapterIndices.size()).append(" Capitulos").toString());

            for(ChapterIndex chapter : chapterIndices ){
                View chapterView = LayoutInflater.from(NovelDetailsActivity.this).inflate(
                        R.layout.chapter_grid_button, null);

                TextView chapterName = (TextView) chapterView.findViewById(R.id.chapter_name);
                chapterName.setText(chapter.getChapterName());

                chapterView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ADD your action here
                        Intent intent = new Intent(ctx, ReaderActivity.class);
                        intent.putExtra("NovelReaderController", new NovelReaderController(chapterIndices));
                        intent.putExtra("chapterLink", chapter.getChapterLink());
                        ctx.startActivity(intent);
                    }
                });

                chapterContainer.addView(chapterView);
            }

            currentNovel = novelDetails;
        }
    }

    private class getNovelDetailsFromDB extends AsyncTask<String, NovelDetails, NovelDetails> {
        private TextView title;
        private TextView author;
        private TextView description;
        private ImageView image;
        private LinearLayout chapterContainer;
        private TextView chapterQuantity;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            title = (TextView) findViewById(R.id.novel_name);
            author = (TextView) findViewById(R.id.novel_author);
            description = (TextView) findViewById(R.id.novel_description);
            image = (ImageView) findViewById(R.id.novel_image);
            chapterContainer = (LinearLayout) findViewById(R.id.novel_container);
            chapterQuantity = (TextView) findViewById(R.id.chapter_quantity);

        }

        @Override
        protected NovelDetails doInBackground(String... novelDetails) {
            DBController db = new DBController(NovelDetailsActivity.this);
            NovelDetails novel = db.getNovel(novelDetails[0], novelDetails[1]);

            publishProgress(novel);

            novel.setChapterIndexes(db.getChaptersFromANovel(novel.getNovelName(), "NovelFull"));

            return novel;
        }


        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            title.setText(novelDetails[0].getNovelName());
            description.setText(novelDetails[0].getNovelDescription());
            author.setText(novelDetails[0].getNovelAuthor());
            image.setImageBitmap(novelDetails[0].getNovelImage());
        }

        @Override
        protected void onPostExecute(NovelDetails novelDetails) {
            super.onPostExecute(novelDetails);
            ArrayList<ChapterIndex> chapterIndices;

            if(novelDetails == null){
                return;
            }

            title.setText(novelDetails.getNovelName());
            description.setText(novelDetails.getNovelDescription());
            author.setText(novelDetails.getNovelAuthor());
            image.setImageBitmap(novelDetails.getNovelImage());

            chapterIndices = novelDetails.getChapterIndexes();

            chapterQuantity.setText(new StringBuilder().append(chapterIndices.size()).append(" Capitulos").toString());

            for(ChapterIndex chapter : chapterIndices ){
                View chapterView = LayoutInflater.from(NovelDetailsActivity.this).inflate(
                        R.layout.chapter_grid_button, null);

                TextView chapterName = (TextView) chapterView.findViewById(R.id.chapter_name);
                chapterName.setText(chapter.getChapterName());

                ImageButton imageButton = (ImageButton) chapterView.findViewById(R.id.downloadButton);

                if(chapter.getDownloaded().equals("no")){
                    imageButton.setImageResource(R.drawable.ic_outline_arrow_circle_down_40);
                }else if(chapter.getDownloaded().equals("downloading")){
                    imageButton.setImageResource(R.drawable.ic_outline_cancel_40);
                }else{
                    imageButton.setImageResource(R.drawable.ic_round_check_circle_40);
                }

                chapterView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ADD your action here
                        Intent intent = new Intent(ctx, ReaderActivity.class);
                        intent.putExtra("NovelReaderController", new NovelReaderController(chapterIndices));
                        intent.putExtra("chapterLink", chapter.getChapterLink());
                        ctx.startActivity(intent);
                    }
                });

                chapterContainer.addView(chapterView);
            }

            currentNovel = novelDetails;
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
}