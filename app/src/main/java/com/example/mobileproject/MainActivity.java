package com.example.mobileproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.epub.BookDetails;
import com.example.mobileproject.model.epub.EpubChapter;
import com.example.mobileproject.model.epub.EpubParser;
import com.example.mobileproject.model.epub.HtmlParser;
import com.example.mobileproject.ui.downloads.DownloadsFragment;
import com.example.mobileproject.ui.library.LibraryFragment;
import com.example.mobileproject.ui.navigate.NavigateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_EPUB_FILE = 321;

    private LibraryFragment libraryFragment;
    private BottomNavigationView bottomNav;

    private TextView loadingState;
    private FrameLayout loadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadView = (FrameLayout) findViewById(R.id.epub_load_screen);
        loadingState = (TextView) loadView.findViewById(R.id.epub_loading_state);

        bottomNav = findViewById(R.id.bottom_navigation);
        //bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });

        libraryFragment = new LibraryFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                libraryFragment).commit();
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener(){

                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_library:
                            selectedFragment = new LibraryFragment();
                            libraryFragment = (LibraryFragment) selectedFragment;
                            break;
                        case R.id.nav_navigate:
                            selectedFragment = new NavigateFragment(MainActivity.this);
                            break;
                        case R.id.nav_download_list:
                            selectedFragment = new DownloadsFragment();
                            break;
                    }
                    if(selectedFragment == null){
                        return true;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                            selectedFragment).commit();

                    return true;
                }
            };

    public void showBottomNav() {
        bottomNav.setVisibility(View.VISIBLE);
    }

    public void hideBottomNav() {
        bottomNav.setVisibility(View.GONE);
    }

    public void openEpubPicker(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/epub+zip");
        String[] mimetypes = {"application/epub+zip"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, PICK_EPUB_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == PICK_EPUB_FILE && resultCode == RESULT_OK){
            if(data == null){
                return;
            }

            ParseEpubAsyncTask parseEpubAsyncTask = new ParseEpubAsyncTask();
            parseEpubAsyncTask.execute(data);


            return;
        }

        if(libraryFragment != null){
            libraryFragment.UpdateNovels();
        }
    }


    private class ParseEpubAsyncTask extends AsyncTask<Intent, Integer, BookDetails>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadView.setVisibility(View.VISIBLE);
            loadingState.setText("Parsing Epub");
        }

        @Override
        protected BookDetails doInBackground(Intent... intents) {
            DBController db = new DBController(MainActivity.this);
            Intent dataIntent = intents[0];
            BookDetails bookDetails;
            NovelDetails novelDetails;

            EpubParser epubParser = new EpubParser(MainActivity.this);

            try {
                bookDetails = epubParser.parse(dataIntent.getData());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            publishProgress(1);

            int index = 1;
            HtmlParser htmlParser = new HtmlParser();
            for (EpubChapter epubChapter : bookDetails.getChapterList()){
                Document chapterRawHtml = Jsoup.parse(epubChapter.getRawChapterContent());

                htmlParser.removeUnnecessaryTags(chapterRawHtml);

                String cleanedChapter = htmlParser.CleanChapter(chapterRawHtml.html());

                epubChapter.setChapterContent(cleanedChapter);

                publishProgress(2, index);

                index++;
            }

            publishProgress(3);

            if(!db.checkIfEpubAlreadyExists(bookDetails)){
                db.insertEpub(bookDetails);
            }

            return bookDetails;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if(values[0] == 1){
                loadingState.setText("Parse Finished");
            }else if(values[0] == 2){
                loadingState.setText("Cleaning chapter " + values[1]);
            }else if(values[0] == 3){
                loadingState.setText("Storing on database");
            }
        }

        @Override
        protected void onPostExecute(BookDetails bookDetails) {
            super.onPostExecute(bookDetails);

            loadView.setVisibility(View.GONE);

            Intent intent = new Intent(MainActivity.this, NovelDetailsActivity.class);

            intent.putExtra("NovelDetails_name", bookDetails.getBookName());
            intent.putExtra("isFavorite", "no");
            intent.putExtra("NovelDetails_source", bookDetails.getBookPublisher());

            startActivityForResult(intent, 1);
        }
    }
}