package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.ui.reader_settings.ReaderSettings;
import com.example.mobileproject.util.FontFactory;

import java.util.ArrayList;


public class ReaderActivity extends AppCompatActivity {
    private NovelReaderController nrc;
    private GetChapterContent getChapterContent;
    Button previousButton;
    Button nextButton;

    private RecyclerView mRecyclerView;
    private ReaderChaptersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<Integer> chaptersReadied = new ArrayList<>();

    private SeekBar mSeekBar;
    private TextView chapterProgress;

    Animation animTranslateIn;
    Animation animTranslateOut;
    Animation animTranslateBottomIn;
    Animation animTranslateBottomOut;
    Animation animTranslateSideIn;
    Animation animTranslateSideOut;

    public ReaderSettings readerSettings;

    private String sourceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        setReaderUserPreferences();

        animTranslateIn = AnimationUtils.loadAnimation(this, R.anim.translate_in);
        animTranslateOut = AnimationUtils.loadAnimation(this, R.anim.translate_out);
        animTranslateBottomIn = AnimationUtils.loadAnimation(this, R.anim.translate_botton_in);
        animTranslateBottomOut = AnimationUtils.loadAnimation(this, R.anim.translate_bottom_out);
        animTranslateSideIn = AnimationUtils.loadAnimation(this, R.anim.translate_side_in);
        animTranslateSideOut = AnimationUtils.loadAnimation(this, R.anim.translate_side_out);


        Intent i = getIntent();
        String chapterLink = i.getStringExtra("chapterLink");
        System.out.println(chapterLink);
        sourceName = i.getStringExtra("sourceName");
        nrc = (NovelReaderController) i.getSerializableExtra("NovelReaderController");
        nrc.setStartedChapter(chapterLink);

        mRecyclerView = findViewById(R.id.reader_menu_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ReaderChaptersAdapter(nrc, chapterLink, this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DownloadReceiver downloadReceiver = new DownloadReceiver(new Handler(), mAdapter);
        mAdapter.setDownloadReceiver(downloadReceiver);


        previousButton = (Button) findViewById(R.id.reader_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreviousChapter();
            }
        });

        nextButton = (Button) findViewById(R.id.reader_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNextChapter();
            }
        });

        setUpMenu();

        TextView textView = (TextView) findViewById(R.id.chapter_content);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
                FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

                if(readerSettings != null){
                    FrameLayout temp = readerSettings.getFrameLayout();
                    RelativeLayout container = (RelativeLayout) findViewById(R.id.reader_activity);
                    container.removeView(temp);
                    readerSettings = null;

                    return;
                }

                if(sideMenu.getVisibility() == View.VISIBLE){
                    sideMenu.startAnimation(animTranslateSideOut);
                    sideMenu.setVisibility(View.GONE);

                    return;
                }

                if (topMenu.getVisibility() == View.GONE){
                    showSystemUI();

                    topMenu.setVisibility(View.VISIBLE);
                    bottomMenu.setVisibility(View.VISIBLE);

                    topMenu.startAnimation(animTranslateIn);
                    bottomMenu.startAnimation(animTranslateBottomIn);
                }else{
                    hideSystemUI();

                    topMenu.startAnimation(animTranslateOut);
                    bottomMenu.startAnimation(animTranslateBottomOut);

                    topMenu.setVisibility(View.GONE);
                    bottomMenu.setVisibility(View.GONE);
                }
            }
        });

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(nrc.getCurrentChapter());
    }

    private void setUpMenu(){
        Intent i = getIntent();
        String novelName = i.getStringExtra("novelName");

        ImageButton returnButton = (ImageButton) findViewById(R.id.reader_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("readiedChapters", chaptersReadied);
                setResult(RESULT_OK,data);

                finish();
            }
        });

        TextView novelNameTextView = (TextView) findViewById(R.id.reader_novel_name);
        novelNameTextView.setText(novelName);

        ImageButton nextButton = (ImageButton) findViewById(R.id.reader_menu_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNextChapter();
            }
        });

        ImageButton previousButton = (ImageButton) findViewById(R.id.reader_menu_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreviousChapter();
            }
        });

        TextView sideMenuNovelName = (TextView) findViewById(R.id.reader_side_menu_name);
        sideMenuNovelName.setText(novelName);

        chapterProgress = (TextView) findViewById(R.id.reader_seekbar_progress);
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        mSeekBar = (SeekBar) findViewById(R.id.novel_progress);
        mSeekBar.setProgress(0);
        mSeekBar.setMax(nrc.getSize() - 1);
        mSeekBar.setProgress(nrc.getPosition());

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                chapterProgress.setText(new StringBuilder().append(i + 1).append("/").append(nrc.getSize()).toString());
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("Go to - ", String.valueOf(progress - 1));

                ChapterIndex c = nrc.goToChapter(progress);

                getChapterContent = new GetChapterContent();
                getChapterContent.execute(c);
            }
        });

        ImageButton openChaptersMenu = (ImageButton) findViewById(R.id.open_chapter_side_menu);
        openChaptersMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);
                FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);

                hideSystemUI();

                topMenu.startAnimation(animTranslateOut);
                bottomMenu.startAnimation(animTranslateBottomOut);

                topMenu.setVisibility(View.GONE);
                bottomMenu.setVisibility(View.GONE);

                mRecyclerView.scrollToPosition(mAdapter.currentReadingPosition);
                sideMenu.setVisibility(View.VISIBLE);
                sideMenu.startAnimation(animTranslateSideIn);
            }
        });

        ImageButton openReaderSettings = (ImageButton) findViewById(R.id.open_reader_settings);
        openReaderSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout topMenu = (FrameLayout) findViewById(R.id.reader_top_menu);
                FrameLayout bottomMenu = (FrameLayout) findViewById(R.id.reader_bottom_menu);

                topMenu.startAnimation(animTranslateOut);
                bottomMenu.startAnimation(animTranslateBottomOut);
                topMenu.setVisibility(View.GONE);
                bottomMenu.setVisibility(View.GONE);

                RelativeLayout container = (RelativeLayout) findViewById(R.id.reader_activity);
                FrameLayout inflatedLayout= (FrameLayout) getLayoutInflater()
                        .inflate(R.layout.reader_settings, container, false);
                readerSettings = new ReaderSettings(inflatedLayout, ReaderActivity.this);
                container.addView(inflatedLayout);

                /*

                FrameLayout readerSettings = (FrameLayout) findViewById(R.id.reader_settings);
                readerSettings.setVisibility(View.VISIBLE);

                 */
            }
        });

        //SetUpReaderConfigMenu();
    }

    private void setReaderUserPreferences(){
        SharedPreferences preferences = getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

        LinearLayout container = findViewById(R.id.reader_container);
        container.setBackgroundColor(Color.parseColor(preferences.getString("background_color", "#1F1B1B")));

        TextView chapterContentView = (TextView) findViewById(R.id.chapter_content);
        chapterContentView.setTextSize(preferences.getFloat("font_size", 20));
        chapterContentView.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), this));
        chapterContentView.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));
        TextView chapterTitleView = (TextView) findViewById(R.id.chapter_name);
        chapterTitleView.setTextSize(preferences.getFloat("font_size", 20) + 15);
        chapterTitleView.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), this));
        chapterTitleView.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));


        System.out.println(preferences.getFloat("font_size", 20));//	#FFFFFF
        System.out.println(preferences.getString("font_name", "Roboto"));//	#FFFFFF
        System.out.println(preferences.getString("font_color", "#FFFFFF"));//	#FFFFFF
        System.out.println(preferences.getString("background_color", "#1F1B1B"));//	#FFFFFF
    }

    public void changeReaderSettings(float font_size, String font_name, String font_color, String background_color){
        SharedPreferences preferences = getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(background_color != null){
            LinearLayout container = findViewById(R.id.reader_container);
            container.setBackgroundColor(Color.parseColor(background_color));
            editor.putString("background_color", background_color);
        }
        TextView chapterContentView = (TextView) findViewById(R.id.chapter_content);
        TextView chapterTitleView = (TextView) findViewById(R.id.chapter_name);

        if(font_size != 0){
            chapterContentView.setTextSize(font_size);
            chapterTitleView.setTextSize(font_size + 15);
            editor.putFloat("font_size", font_size);
        }

        if(font_name != null){
            chapterContentView.setTypeface(new FontFactory().GetFont(font_name, this));
            chapterTitleView.setTypeface(new FontFactory().GetFont(font_name, this));
            editor.putString("font_name", font_name);
        }

        if(font_color != null) {
            chapterContentView.setTextColor(Color.parseColor(font_color));
            chapterTitleView.setTextColor(Color.parseColor(font_color));
            editor.putString("font_color", font_color);
        }

        editor.apply();
    }

    /*
    private void SetUpReaderConfigMenu(){
        TextView textPreview = (TextView) findViewById(R.id.text_preview);
        LinearLayout bgPreview = (LinearLayout) findViewById(R.id.background_preview);

        //reader_font_size_selector
        Spinner dropdown = findViewById(R.id.reader_font_size_selector);
        String[] items = new String[]{"16", "17","18","19","20","21","22","23","24","25","26","27","28","29","30"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                textPreview.setTextSize((Float) Float.parseFloat((String) parent.getItemAtPosition(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner dropdown2 = findViewById(R.id.reader_font_family_selector);
        String[] items2 = new String[]{"Acme", "Alice", "Coming_soon", "Roboto"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items2);
        dropdown2.setAdapter(adapter2);
        dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Typeface font = null;

                Log.i("item: ", (String) parent.getItemAtPosition(position));

                String font_name = (String) parent.getItemAtPosition(position);

                if(font_name.equals("Acme")){
                    font = ResourcesCompat.getFont(ReaderActivity.this, R.font.acme);
                    textPreview.setTypeface(font);
                }else if(font_name.equals("Alice")){
                    font = ResourcesCompat.getFont(ReaderActivity.this, R.font.alice);
                    textPreview.setTypeface(font);
                }else if(font_name.equals("Coming_soon")){
                    font = ResourcesCompat.getFont(ReaderActivity.this, R.font.coming_soon);
                    textPreview.setTypeface(font);
                }else if(font_name.equals("Roboto")){
                    font = ResourcesCompat.getFont(ReaderActivity.this, R.font.roboto);
                    textPreview.setTypeface(font);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        LinearLayout mColorChanger = findViewById(R.id.reader_font_color_selector);
        mColorChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog= ColorPickerDialog.createColorPickerDialog(ReaderActivity.this,ColorPickerDialog.DARK_THEME);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        textPreview.setTextColor(Color.parseColor(hexVal));
                        mColorChanger.setBackgroundColor(Color.parseColor(hexVal));
                    }
                });
                colorPickerDialog.show();
            }
        });

        LinearLayout mBgChanger = findViewById(R.id.reader_bg_color_selector);
        bgPreview.setBackgroundColor(getResources().getColor(R.color.main_dark_theme));
        mBgChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog= ColorPickerDialog.createColorPickerDialog(ReaderActivity.this,ColorPickerDialog.DARK_THEME);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        bgPreview.setBackgroundColor(Color.parseColor(hexVal));
                        mBgChanger.setBackgroundColor(Color.parseColor(hexVal));
                    }
                });
                colorPickerDialog.show();
            }
        });
    }
*/
    private void getPreviousChapter(){
        if(getChapterContent.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }

        ChapterIndex previous = nrc.getPreviousChapter();

        if(previous == null){
            Toast.makeText(this, "Não tem capitulo anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());


        getChapterContent = new GetChapterContent();
        getChapterContent.execute(previous);
    }

    private void getNextChapter() {
        if(getChapterContent.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }
        int currentChapter = nrc.getCurrentChapter().getId();

        ChapterIndex next = nrc.getNextChapter();

        if(next == null){
            Toast.makeText(this, "Não tem capitulo anterior", Toast.LENGTH_SHORT).show();
            return;
        }


        if(currentChapter != -1 && !chaptersReadied.contains(currentChapter)){
            chaptersReadied.add(currentChapter);
            DBController db = new DBController(this);
            db.setChapterAsReaded(currentChapter);
        }

        mSeekBar.setProgress(nrc.getPosition());
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(next);
    }

    public void goTo(ChapterIndex c){
        FrameLayout sideMenu = (FrameLayout) findViewById(R.id.reader_side_menu);
        int currentPosition = nrc.getPosition();

        if(sideMenu.getVisibility() == View.VISIBLE){
            sideMenu.startAnimation(animTranslateSideOut);
            sideMenu.setVisibility(View.GONE);
        }

        mSeekBar.setProgress(currentPosition);
        chapterProgress.setText(new StringBuilder().append(nrc.getPosition()).append("/").append(nrc.getSize()).toString());

        getChapterContent = new GetChapterContent();
        getChapterContent.execute(c);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("readiedChapters", chaptersReadied);
        setResult(RESULT_OK,data);

        finish();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private class GetChapterContent extends AsyncTask<ChapterIndex, Void, ChapterContent> {
        private TextView chapterName;
        private TextView chapterNameBottom;
        private TextView chapterContentView;

        private ScrollView scrollView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            chapterName = (TextView) findViewById(R.id.chapter_name);
            chapterNameBottom = (TextView) findViewById(R.id.chapter_name_bottom);
            chapterContentView = (TextView) findViewById(R.id.chapter_content);

            scrollView = (ScrollView) findViewById(R.id.reader_scroll_view);
        }

        @Override
        protected ChapterContent doInBackground(ChapterIndex... chapter) {
            ChapterContent chapterContent;

            if(chapter[0].getDownloaded().equals("yes") || chapter[0].getDownloaded().equals("downloading")){
                DBController db = new DBController(ReaderActivity.this);
                chapterContent = db.getChapter(chapter[0].getId());

                if(!chapterContent.getChapterContent().isEmpty()){
                    return chapterContent;
                }
            }

            ParserInterface parser = ParserFactory.getParserInstance(sourceName,ReaderActivity.this);
            if(parser == null){
                return null;
            }

            chapterContent = parser.getChapterContent(chapter[0].getChapterLink());

            return chapterContent;
        }

        @Override
        protected void onPostExecute(ChapterContent chapterContent) {
            super.onPostExecute(chapterContent);

            if(chapterContent == null || chapterContent.getChapterContent().isEmpty()){
                Toast.makeText(ReaderActivity.this, "Falha ao acessar", Toast.LENGTH_SHORT).show();
                return;
            }

            chapterName.setText(chapterContent.getChapterName());
            chapterNameBottom.setText(chapterContent.getChapterName());
            chapterContentView.setText(chapterContent.getChapterContent());

            scrollView.fullScroll(ScrollView.FOCUS_UP);
            scrollView.pageScroll(ScrollView.FOCUS_UP);
            scrollView.smoothScrollTo(0,0);
        }
    }

}

