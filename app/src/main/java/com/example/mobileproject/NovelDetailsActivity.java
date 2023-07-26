package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
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
import com.example.mobileproject.util.ServiceHelper;

import java.lang.reflect.Array;
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
    private boolean atLeastOneChapterReadied = false;

    private getNovelDetailsFromDB contentDB;
    private getNovelDetails content;

    Context ctx = this;

    NovelDetails currentNovel = null;


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
                Intent resultIntent = new Intent();
                resultIntent.putExtra("atLeastOneChapterReadied", atLeastOneChapterReadied);
                setResult(RESULT_OK, resultIntent);

                if(content != null && content.getStatus() == AsyncTask.Status.RUNNING){
                    content.cancel(true);
                }
                if(contentDB != null && contentDB.getStatus() == AsyncTask.Status.RUNNING){
                    contentDB.cancel(true);
                }

                finish();
            }
        });

        ImageButton simpleButton3 = (ImageButton) findViewById(R.id.downloadAll);
        simpleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                deleteNovelTask d = new deleteNovelTask();
                d.execute();*/
                //downloadAll();
            }
        });

        Intent i = getIntent();
        String novel_name = (String) i.getStringExtra("NovelDetails_name");
        if(novel_name != null){
            novel_name = novel_name.replace("'", "");
        }
        String novel_source = (String) i.getStringExtra("NovelDetails_source");

        if(novel_name != null && novel_source != null){
            contentDB = new getNovelDetailsFromDB();
            contentDB.execute(novel_name, novel_source, "");
        }else{
            String novelLink = i.getStringExtra("novelLink");
            String novelName = i.getStringExtra("novelName").replace("'", "");
            String novelSource = i.getStringExtra("novelSource");
            String alternativeUrl = i.getStringExtra("alternativeUrl");

            System.out.println(alternativeUrl);
            if(alternativeUrl == null){
                alternativeUrl = "";
            }

            content = new getNovelDetails();
            content.execute(novelLink, novelName, novelSource, alternativeUrl);
        }

        ImageButton imageButton = findViewById(R.id.shareButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentNovel == null){
                    return;
                }

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);

                String shareTitle = "Leia a Novel " + currentNovel.getNovelName();
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, currentNovel.getNovelDescription());
                startActivity(Intent.createChooser(intent, "Share via"));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            ArrayList<Integer> readiedChapters = data.getIntegerArrayListExtra("readiedChapters");
            if(!readiedChapters.isEmpty()){
                atLeastOneChapterReadied = true;
            }
            mAdapter.putChapterAsReadied(readiedChapters);

            ArrayList<Integer> downloadedChapters = data.getIntegerArrayListExtra("downloadedChapters");
            mAdapter.putChapterAsDownloaded(downloadedChapters);

            currentNovel.setReaderViewType(data.getIntExtra("currentReaderViewType", 1));
        }

    }

    public void openSelectMenu(ArrayList<ChapterIndex> reference){
        selectedChaptersReference = reference;

        showSelectMenu();

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

                atLeastOneChapterReadied = true;

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

                atLeastOneChapterReadied = true;

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

        ImageButton deleteChapterContentSelected = (ImageButton) findViewById(R.id.delete_chapter_content);
        deleteChapterContentSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.deleteSelectedChapters();
                hideSelectMenu();

                DeleteMultipleChapterContentsTask task = new DeleteMultipleChapterContentsTask();
                task.execute();
            }
        });

        update();
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

        TextView selectedQuantity = (TextView) findViewById(R.id.selected_quantity_n);
        selectedQuantity.setText(String.valueOf(selectedChaptersReference.size()));

        if(selectedChaptersReference.isEmpty()){
            hideSelectMenu();
        }
    }

    private boolean checkIfThereIsAnNoReadiedChapter(){
        for(ChapterIndex c : selectedChaptersReference){
            if(c.getReaded().equals("no")){
                return true;
            }
        }

        return false;
    }

    private void showSelectMenu(){
        View topView1 = (LinearLayout) findViewById(R.id.normal_menu);
        View topView2 = (LinearLayout) findViewById(R.id.select_menu);
        View bottomMenuNormal = (FrameLayout) findViewById(R.id.bottom_normal_menu);
        View bottomMenuSelect = (FrameLayout) findViewById(R.id.bottom_select_menu);
        topView1.setVisibility(View.GONE);
        topView2.setVisibility(View.VISIBLE);
        bottomMenuNormal.setVisibility(View.GONE);
        bottomMenuSelect.setVisibility(View.VISIBLE);
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
        Intent resultIntent = new Intent();
        resultIntent.putExtra("atLeastOneChapterReadied", atLeastOneChapterReadied);
        setResult(RESULT_OK,resultIntent);

        if(content != null && content.getStatus() == AsyncTask.Status.RUNNING){
            content.cancel(true);
        }
        if(contentDB != null && contentDB.getStatus() == AsyncTask.Status.RUNNING){
            contentDB.cancel(true);
        }

        finish();
    }

    public void updateChapterList(String novelName, String novelSource){
        if(currentNovel.getNovelName().equals(novelName) && currentNovel.getSource().equals(novelSource)){

            UpdateChaptersList updateChaptersList = new UpdateChaptersList();
            updateChaptersList.execute();

            new Thread(new CheckForDownloadServiceRunningTask()).start();
        }
    }

    private class CheckForDownloadServiceRunningTask implements Runnable {

        @Override
        public void run() {
            boolean isServiceRunning = false;
            while (!isServiceRunning){
                if(Thread.currentThread().isInterrupted())
                    break;

                isServiceRunning = ServiceHelper.isMyServiceRunning(ctx, DownloaderService.class);

                if(isServiceRunning){
                    Intent serviceIntent = new Intent(ctx, DownloaderService.class);
                    serviceIntent.putExtra("receiver", (Parcelable) downloadReceiver);
                    ctx.startService(serviceIntent);
                }

                SystemClock.sleep(1000);
            }
        }
    }

    private class getNovelDetails extends AsyncTask<String, NovelDetails, ArrayList<ChapterIndex>> {

        private String novelName, novelSource, novelLink;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(String... novelDetails) {
            NovelDetails n = null;
            long id;
            DBController db = new DBController(ctx);

            //1641068613060

            if(novelDetails.length > 1){
                if(!novelDetails[1].isEmpty()){
                    n = db.getNovel(novelDetails[1], novelDetails[2]);
                }else if(!novelDetails[0].isEmpty()){
                    n = db.getNovelWithNovelLink(novelDetails[2], novelDetails[0]);
                }
            }

            if(n != null){
                if( ((System.currentTimeMillis() - n.getLastReadied()) < (60 * 60 * 1000) && n.getFinishedLoading() == 1)
                        || n.getIsFavorite().equals("yes")){
                    novelLink = novelDetails[0];
                    novelName = novelDetails[1];
                    novelSource = novelDetails[2];

                    return null;
                }

                db.deleteNovel(n.getNovelName(), n.getSource());
            }
            ParserInterface parser;

            if(!novelDetails[3].equals("")){
                parser = ParserFactory.getParserInstanceWithAlternativeUrl(novelDetails[3], ctx);
            }else{
                parser = ParserFactory.getParserInstance(novelDetails[2], ctx);
            }

            if (parser == null) {
                return null;
            }

            n = parser.getNovelDetails(novelDetails[0]);
            n.setLastPageSearched(parser.getLastPageSearched());

            try {
                id = db.insertNovel(n);
            }catch (SQLiteConstraintException e){
                novelName = null;
                return null;
            }

            n.setDb_id((int) id);

            publishProgress(n);

            ArrayList<ChapterIndex> c = parser.getAllChaptersIndex(novelDetails[0]);

            for(ChapterIndex _c : c){
                db.insertChapters(n.getNovelName(), n.getSource(), _c);
            }

            db.finishedLoading(n.getNovelName(), n.getSource(), parser.getLastPageSearched());

            return db.getChaptersFromANovel(n.getNovelName(), n.getSource());
        }

        @Override
        protected void onProgressUpdate(NovelDetails... novelDetails){
            if(currentNovel != null && currentNovel.equals(novelDetails[0])){
                return;
            }
            currentNovel = novelDetails[0];
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
                contentDB.execute(novelName, novelSource, novelLink);
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
        }
    }

    private class getNovelDetailsFromDB extends AsyncTask<String, NovelDetails, ArrayList<ChapterIndex>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(String... novelDetails) {
            NovelDetails novel;
            DBController db = new DBController(NovelDetailsActivity.this);
            if(novelDetails[0] == null || novelDetails[1] == null){
                return null;
            }

            if(!novelDetails[0].isEmpty()){
                novel = db.getNovel(novelDetails[0], novelDetails[1]);
            }else if (!novelDetails[2].isEmpty()){
                novel = db.getNovelWithNovelLink(novelDetails[1], novelDetails[2]);
            }else{
                return null;
            }

            if(novel == null){
                return null;
            }

            publishProgress(novel);

            ArrayList<ChapterIndex> c = db.getChaptersFromANovel(novel.getNovelName(), novel.getSource());

            ArrayList<RepeatedChapter> repeatedChaptersToRemove = detectRepeatedClasses(c);

            for (RepeatedChapter rc : repeatedChaptersToRemove){
                for (ChapterIndex repeatedChapter : rc.repeatedChapters){
                    c.remove(repeatedChapter);
                }
            }

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

        private ArrayList<RepeatedChapter> detectRepeatedClasses(ArrayList<ChapterIndex> chapterIndexes){
            ArrayList<RepeatedChapter> repeatedChapters = new ArrayList<>();

            for(ChapterIndex c : chapterIndexes){
                boolean alreadyRepeated = false;
                RepeatedChapter currentCheckingChapter = new RepeatedChapter(c);

                for(RepeatedChapter rc : repeatedChapters){
                    if (c.getId() == rc.currentChapter.getId()){
                        alreadyRepeated = true;
                        break;
                    }

                    for(ChapterIndex rc1 : rc.repeatedChapters){
                        if(c.getId() == rc1.getId()){
                            alreadyRepeated = true;
                            break;
                        }
                    }
                }

                if (alreadyRepeated){
                    continue;
                }

                for (ChapterIndex c1 : chapterIndexes){
                    if(c.getId() == c1.getId()){
                        continue;
                    }

                    if(
                            c.getChapterName().equals(c1.getChapterName())
                            || c.getChapterLink().equals(c1.getChapterLink())
                    ){
                        currentCheckingChapter.repeatedChapters.add(c1);
                    }
                }

                repeatedChapters.add(currentCheckingChapter);
            }

            return repeatedChapters;
        }

        private class RepeatedChapter {
            public ChapterIndex currentChapter;
            public ArrayList<ChapterIndex> repeatedChapters = new ArrayList<>();

            public RepeatedChapter(ChapterIndex currentChapter) {
                this.currentChapter = currentChapter;
            }
        }
    }

    private class UpdateChaptersList extends AsyncTask<Void, Void, ArrayList<ChapterIndex>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList<ChapterIndex> doInBackground(Void... voids) {
            DBController db = new DBController(NovelDetailsActivity.this);

            ArrayList<ChapterIndex> c = db.getChaptersFromANovel(currentNovel.getNovelName(), currentNovel.getSource());

            return c;
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> chapterIndexes) {
            super.onPostExecute(chapterIndexes);

            if(chapterIndexes == null){
                return;
            }

            mAdapter.updateChapterList(chapterIndexes);
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

    private class DeleteMultipleChapterContentsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DBController db = new DBController(ctx);

            db.DeleteMultipleChaptersContent(new ArrayList<>(selectedChaptersReference));

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
        }
    }


    private class deleteNovelTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBController db = new DBController(NovelDetailsActivity.this);

            db.deleteNovel(currentNovel.getNovelName(), currentNovel.getSource());

            return null;
        }
    }
}