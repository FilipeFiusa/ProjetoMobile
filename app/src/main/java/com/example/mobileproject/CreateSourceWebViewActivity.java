package com.example.mobileproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.ImageWebViewResource;
import com.example.mobileproject.model.NovelDetails;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javassist.bytecode.analysis.Frame;

public class CreateSourceWebViewActivity extends AppCompatActivity {

    private URL sourceURL;
    private WebView webView;

    private final NovelDetails novelDetails = new NovelDetails();

    private boolean finishedLoading = false;
    protected String html;

    private RecyclerView mRecyclerView;
    private WebViewImagesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_source_web_view_activity);

        Intent i = getIntent();

        sourceURL = (URL) i.getSerializableExtra("currentUrl");

        webView = (WebView) findViewById(R.id.create_source_web_view);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                finishedLoading = true;
                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        });

        webView.loadUrl(sourceURL.toString());

        ImageButton returnButton = (ImageButton) findViewById(R.id.reader_web_view_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView currentLink = (TextView) findViewById(R.id.current_link);
        currentLink.setText(sourceURL.toString());

        ImageButton popupButton = (ImageButton) findViewById(R.id.pop_up_menu_button);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(CreateSourceWebViewActivity.this, popupButton);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.item1:
                                if(finishedLoading){
                                    setUpNovelCreator();
                                    return true;
                                }

                                Toast.makeText(CreateSourceWebViewActivity.this, "Espere o site terminar de carregar", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.item2:
                                Toast.makeText(CreateSourceWebViewActivity.this, "Item2", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.item3:
                                Toast.makeText(CreateSourceWebViewActivity.this, "Item3", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.create_source_popup_menu);
                popupMenu.show();
            }
        });
    }

    public void setUpNovelCreator(){
        FrameLayout novelCreator = (FrameLayout) findViewById(R.id.novel_creator_menu);
        FrameLayout imageSelector = (FrameLayout) findViewById(R.id.image_selector);
        FrameLayout warningMenu = (FrameLayout) findViewById(R.id.next_step_warning);

        novelCreator.setVisibility(View.VISIBLE);

        Button hideButton = (Button) findViewById(R.id.hide_button);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                novelCreator.setVisibility(View.GONE);
            }
        });

        Button cancelButton2 = (Button) findViewById(R.id.cancel_button2);
        cancelButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelector.setVisibility(View.GONE);
            }
        });

        Button okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warningMenu.setVisibility(View.GONE);
            }
        });

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Ongoing", "Completed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        ImageView novelImage = (ImageView) findViewById(R.id.novel_image);
        novelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelector.setVisibility(View.VISIBLE);
            }
        });

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText novelName = (EditText) findViewById(R.id.novel_name_input);
                novelDetails.setNovelName(novelName.getText().toString());

                EditText novelAuthor = (EditText) findViewById(R.id.author_name_input);
                novelDetails.setNovelAuthor(novelAuthor.getText().toString());

                EditText novelDescription = (EditText) findViewById(R.id.novel_description_input);
                novelDetails.setNovelDescription(novelDescription.getText().toString());

                String novelStatus = dropdown.getSelectedItem().toString();
                if(novelStatus.equals("Ongoing")){
                    novelDetails.setStatus(1);
                }else if(novelStatus.equals("Completed")){
                    novelDetails.setStatus(2);
                }

                novelCreator.setVisibility(View.GONE);
                warningMenu.setVisibility(View.VISIBLE);
            }
        });



        Document document = Jsoup.parse(html);
        Elements imageElements = document.select("img");
        ArrayList<ImageWebViewResource> imageResources = new ArrayList<>();
        int maxCount = 0;

        for(Element element : imageElements){
            imageResources.add(new ImageWebViewResource(
                    element.absUrl("src"),
                    element.attr("alt")
            ));

            maxCount++;
            if (maxCount > 10){
                break;
            }
        }

        mRecyclerView = findViewById(R.id.image_selector_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new WebViewImagesAdapter(imageResources, this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        downloadImagesTask task = new downloadImagesTask();
        task.execute(imageResources);
    }

    public void updateCurrentImage(Bitmap image){
        FrameLayout imageSelector = (FrameLayout) findViewById(R.id.image_selector);
        imageSelector.setVisibility(View.GONE);

        ImageView novelImage = (ImageView) findViewById(R.id.novel_image);
        novelImage.setImageBitmap(image);

        novelDetails.setNovelImage(image);
    }


    /* An instance of this class will be registered as a JavaScript interface */
    public static class MyJavaScriptInterface {
        CreateSourceWebViewActivity ctx;

        public MyJavaScriptInterface(CreateSourceWebViewActivity ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            // process the html as needed by the app
            ctx.html = html;
        }
    }

    public class downloadImagesTask extends AsyncTask<ArrayList<ImageWebViewResource>, ImageWebViewResource, Void>{
        @Override
        protected Void doInBackground(ArrayList<ImageWebViewResource>... arrayLists) {
            ArrayList<ImageWebViewResource> images = arrayLists[0];
            InputStream input = null;

            for (ImageWebViewResource image : images){
                try {
                    input = new URL(image.getImageResourceLink()).openStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeStream(input);
                image.setImageResource(bitmap);

                publishProgress(image);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(ImageWebViewResource... values) {
            super.onProgressUpdate(values);

            ImageWebViewResource currentImageResource = values[0];

            mAdapter.notifyItemChanged(currentImageResource.position);
        }
    }

}
