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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.Objects;

public class NovelDetailsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DownloadReceiver downloadReceiver;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<ChapterIndex> selectedChaptersReference;

    private boolean CanUnRead = false;

    private getNovelDetailsFromDB contentDB;
    private getNovelDetails content;

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

        ImageButton simpleButton1 = (ImageButton) findViewById(R.id.font_button_id);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                setResult(RESULT_OK,data);

                if(content != null && content.getStatus() == AsyncTask.Status.RUNNING){
                    content.cancel(true);
                }
                if(contentDB != null && contentDB.getStatus() == AsyncTask.Status.RUNNING){
                    contentDB.cancel(true);
                }

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
            contentDB = new getNovelDetailsFromDB();
            contentDB.execute(novel_name, novel_source);
        }else{
            String novelLink = i.getStringExtra("novelLink");
            String novelName = i.getStringExtra("novelName");
            String novelSource = i.getStringExtra("novelSource");
            content = new getNovelDetails();
            content.execute(novelLink, novelName, novelSource);
        }

        ImageButton imageButton = findViewById(R.id.shareButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentNovel == null){
                    return;
                }

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

    public void openSelectMenu(ArrayList<ChapterIndex> reference){
        selectedChaptersReference = reference;

        View topView1 = (LinearLayout) findViewById(R.id.normal_menu);
        topView1.setVisibility(View.GONE);

        View topView2 = (LinearLayout) findViewById(R.id.select_menu);
        topView2.setVisibility(View.VISIBLE);

        View bottomMenuNormal = (FrameLayout) findViewById(R.id.bottom_normal_menu);
        bottomMenuNormal.setVisibility(View.GONE);

        View bottomMenuSelect = (FrameLayout) findViewById(R.id.bottom_select_menu);
        bottomMenuSelect.setVisibility(View.VISIBLE);

        ImageButton cancelSelection = (ImageButton) findViewById(R.id.cancel_selection);
        cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSelectMenu();

                mAdapter.resetSelectedList();
                selectedChaptersReference = null;
            }
        });

        ImageButton readSelected = (ImageButton) findViewById(R.id.read_selected);
        if(checkIfThereIsAnNoReadiedChapter()){
            readSelected.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_done_20));
            CanUnRead = false;
        }else{
            readSelected.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_done_outline_20));
            CanUnRead = true;
        }
        readSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSelectMenu();

                if(!CanUnRead){
                    SetChaptersReadied task = new SetChaptersReadied();
                    task.execute();

                    mAdapter.readSelectedItems();
                }else{
                    SetChaptersUnReadied task = new SetChaptersUnReadied();
                    task.execute();

                    mAdapter.unreadSelectedItems();
                }
            }
        });
        ImageButton readBellowSelected = (ImageButton) findViewById(R.id.read_bellow_selected);
        readBellowSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lowerSourceId = -1;
                int lowerPosition = -1;

                for(ChapterIndex c : selectedChaptersReference){
                    if(c.getSourceId() < lowerSourceId || lowerSourceId == -1){
                        lowerSourceId = c.getSourceId();
                        lowerPosition = c.position;
                    }
                }

                mAdapter.readAllAntecedents(lowerPosition);

                SetAntecedentChaptersAsReadied setAntecedentChaptersAsReadied = new SetAntecedentChaptersAsReadied();
                setAntecedentChaptersAsReadied.execute(lowerSourceId);
            }
        });

        ImageButton selectAll = (ImageButton) findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectAll();
            }
        });

        ImageButton invertSelection = (ImageButton) findViewById(R.id.invert_selection);
        invertSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.invertSelection();
            }
        });

        ImageButton bookmarkSelected = (ImageButton) findViewById(R.id.bookmark_selected);
        bookmarkSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NovelDetailsActivity.this, "Não Disponivel ainda", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton downloadSelected = (ImageButton) findViewById(R.id.download_selected);
        downloadSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PutMultipleChaptersToDownload putMultipleChaptersToDownload = new PutMultipleChaptersToDownload();
                putMultipleChaptersToDownload.execute();
            }
        });
    }

    public void update(){
        ImageButton readSelected = (ImageButton) findViewById(R.id.read_selected);
        if(checkIfThereIsAnNoReadiedChapter()){
            readSelected.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_done_20));
            CanUnRead = false;
        }else{
            readSelected.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_done_outline_20));
            CanUnRead = true;
        }

        TextView selectedQuantity = (TextView) findViewById(R.id.selected_quantity);
        selectedQuantity.setText(String.valueOf(selectedChaptersReference.size()));

        if(selectedChaptersReference.isEmpty()){
            hideSelectMenu();
        }

        System.out.println(selectedChaptersReference.size());
    }

    private boolean checkIfThereIsAnNoReadiedChapter(){
        for(ChapterIndex c : selectedChaptersReference){
            if(c.getReaded().equals("no")){
                return true;
            }
        }

        return false;
    }

    private void hideSelectMenu(){
        View topView1 = (LinearLayout) findViewById(R.id.normal_menu);
        View topView2 = (LinearLayout) findViewById(R.id.select_menu);
        View bottomMenuNormal = (FrameLayout) findViewById(R.id.bottom_normal_menu);
        View bottomMenuSelect = (FrameLayout) findViewById(R.id.bottom_select_menu);

        topView1.setVisibility(View.VISIBLE);
        topView2.setVisibility(View.GONE);
        bottomMenuNormal.setVisibility(View.VISIBLE);
        bottomMenuSelect.setVisibility(View.GONE);
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

        if(content.getStatus() == AsyncTask.Status.RUNNING){
            content.cancel(true);
        }
        if(contentDB.getStatus() == AsyncTask.Status.RUNNING){
            contentDB.cancel(true);
        }

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
                contentDB = new getNovelDetailsFromDB();
                contentDB.execute(novelName, novelSource);
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

    private class SetChaptersReadied extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DBController db = new DBController(NovelDetailsActivity.this);

            for(ChapterIndex c : selectedChaptersReference){
                db.setChapterAsReaded(c.getId());
            }

            return null;
        }
    }

    private class SetChaptersUnReadied extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DBController db = new DBController(NovelDetailsActivity.this);

            for(ChapterIndex c : selectedChaptersReference){
                db.setChapterAsUnReaded(c.getId());
            }

            return null;
        }
    }

    private class SetAntecedentChaptersAsReadied extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            DBController db = new DBController(NovelDetailsActivity.this);
            int lowerSourceId = integers[0];

            db.SetAntecedentChaptersAsReadied(currentNovel.getNovelName(), currentNovel.getSource(),
                    lowerSourceId);

            return null;
        }
    }

    private class PutMultipleChaptersToDownload extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            DBController db = new DBController(ctx);

            return db.PutMultipleChaptersToDownload(currentNovel.getNovelName(),
                    currentNovel.getSource(), new ArrayList<>(selectedChaptersReference));
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            selectedChaptersReference = new ArrayList<>();

            mAdapter.downloadSelectedItems();

            if(aBoolean){
                Intent serviceIntent = new Intent(ctx, DownloaderService.class);
                serviceIntent.putExtra("NovelName", currentNovel.getNovelName());
                serviceIntent.putExtra("Source", currentNovel.getSource());
                serviceIntent.putExtra("receiver", (Parcelable) downloadReceiver);
                ctx.startService(serviceIntent);
            }
        }
    }
}