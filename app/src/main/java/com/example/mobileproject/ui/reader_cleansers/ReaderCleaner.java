package com.example.mobileproject.ui.reader_cleansers;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.NovelCleaner;
import com.example.mobileproject.model.NovelDetails;

import java.util.ArrayList;

public class ReaderCleaner {
    private final FrameLayout ReaderSettings;
    private final ReaderActivity ctx;
    private final ArrayList<NovelCleaner> novelCleaners;

    private int type; //1 - Any \ 2 - SourceSpecific \ 3 - Novelspecific
    private int layoutId;

    private View mainLayout;
    private View selectLayout;
    private View editLayout;

    private RecyclerView mRecyclerView;
    private CleanerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String novelName;
    private String novelSource;

    public ReaderCleaner(
            FrameLayout frameLayout,
            ReaderActivity ctx,
            ArrayList<NovelCleaner> novelCleaners,
            String novelName,
            String novelSource
    ) {
        this.ReaderSettings = frameLayout;
        this.ctx = ctx;
        this.novelCleaners = novelCleaners;

        this.novelName = novelName;
        this.novelSource = novelSource;

        SetUpReaderConfigMenu();
    }

    private void SetUpReaderConfigMenu() {
        mainLayout = ReaderSettings.findViewById(R.id.main_layout);
        selectLayout = ReaderSettings.findViewById(R.id.select_layout);
        editLayout = ReaderSettings.findViewById(R.id.edit_layout);


        ImageButton returnButton = (ImageButton) ReaderSettings.findViewById(R.id.return_layout);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnLayout();
            }
        });

        ImageButton closeReaderCleaner = (ImageButton) ReaderSettings.findViewById(R.id.close_reader_settings);
        closeReaderCleaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        SetUpMainLayout();
    }


    private void SetUpMainLayout(){
        layoutId = 1;

        mainLayout.setVisibility(View.VISIBLE);
        selectLayout.setVisibility(View.GONE);
        editLayout.setVisibility(View.GONE);

        View anyButton = (View) ReaderSettings.findViewById(R.id.any_button);
        anyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetUpSelectLayout(1);
                Toast.makeText(ctx, "anyButton clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View specificButton = (View) ReaderSettings.findViewById(R.id.specific_button);
        specificButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetUpSelectLayout(2);
                Toast.makeText(ctx, "specificButton clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View novelSpecificButton = (View) ReaderSettings.findViewById(R.id.novel_specific_button);
        novelSpecificButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetUpSelectLayout(3);
                Toast.makeText(ctx, "novelSpecificButton clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void SetUpSelectLayout(int type){
        layoutId = 2;
        this.type = type;

        mainLayout.setVisibility(View.GONE);
        selectLayout.setVisibility(View.VISIBLE);
        editLayout.setVisibility(View.GONE);

        mRecyclerView = ReaderSettings.findViewById(R.id.cleaner_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(ctx);

        mAdapter = new CleanerAdapter(novelCleaners, ctx, type, this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ImageButton addButton = (ImageButton) ReaderSettings.findViewById(R.id.add_cleaner);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetUpEditLayout();
            }
        });
    }

    private void SetUpEditLayout(){
        layoutId = 3;

        mainLayout.setVisibility(View.GONE);
        selectLayout.setVisibility(View.GONE);
        editLayout.setVisibility(View.VISIBLE);

        Button createButton = ReaderSettings.findViewById(R.id.create_cleaner);
        createButton.setText("Criar");
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameInput = (EditText) ReaderSettings.findViewById(R.id.name_input);
                EditText flagInput = (EditText) ReaderSettings.findViewById(R.id.flag_input);
                EditText replacementInput = (EditText) ReaderSettings.findViewById(R.id.replacement_input);

                NovelCleaner cleaner = new NovelCleaner(
                        nameInput.getText().toString(),
                        flagInput.getText().toString(),
                        replacementInput.getText().toString(),
                        type
                );

                nameInput.getText().clear();
                flagInput.getText().clear();
                replacementInput.getText().clear();

                //CreateCleaner(cleaner);

                CreateCleanerTask createCleanerTask = new CreateCleanerTask();
                createCleanerTask.execute(cleaner);
            }
        });
    }

    public void EditCleaner(NovelCleaner cleaner){
        layoutId = 3;

        mainLayout.setVisibility(View.GONE);
        selectLayout.setVisibility(View.GONE);
        editLayout.setVisibility(View.VISIBLE);

        EditText nameInput = (EditText) ReaderSettings.findViewById(R.id.name_input);
        nameInput.setText(cleaner.getName());
        EditText flagInput = (EditText) ReaderSettings.findViewById(R.id.flag_input);
        flagInput.setText(cleaner.getFlag());
        EditText replacementInput = (EditText) ReaderSettings.findViewById(R.id.replacement_input);
        replacementInput.setText(cleaner.getReplacement());

        Button createButton = ReaderSettings.findViewById(R.id.create_cleaner);
        createButton.setText("Atualizar");
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleaner.setName(nameInput.getText().toString());
                cleaner.setFlag(flagInput.getText().toString());
                cleaner.setReplacement(replacementInput.getText().toString());

                nameInput.getText().clear();
                flagInput.getText().clear();
                replacementInput.getText().clear();

                EditCleanerTask editCleanerTask = new EditCleanerTask();
                editCleanerTask.execute(cleaner);
            }
        });
    }

    private void CreateCleaner(NovelCleaner cleaner){
        layoutId = 2;

        mainLayout.setVisibility(View.GONE);
        selectLayout.setVisibility(View.VISIBLE);
        editLayout.setVisibility(View.GONE);

        novelCleaners.add(cleaner);
        mAdapter.addCleaner(cleaner);
    }

    private void UpdateCleaner(NovelCleaner cleaner){
        layoutId = 2;

        mainLayout.setVisibility(View.GONE);
        selectLayout.setVisibility(View.VISIBLE);
        editLayout.setVisibility(View.GONE);

        mAdapter.notifyItemChanged(cleaner.position);
    }

    private void close(){
        RelativeLayout container = (RelativeLayout) ctx.findViewById(R.id.reader_activity);
        container.removeView(ReaderSettings);
        ctx.readerCleaner = null;

    }

    private void returnLayout(){
        if (layoutId == 1) {
            close();
        }else if(layoutId == 2){
            SetUpMainLayout();
        }else if(layoutId == 3){
            SetUpSelectLayout(type);
        }
    }


    private class CreateCleanerTask extends AsyncTask<NovelCleaner, Void, NovelCleaner> {
        @Override
        protected NovelCleaner doInBackground(NovelCleaner... novelCleaners) {
            DBController db = new DBController(ctx);
            NovelCleaner cleaner = novelCleaners[0];

            if(cleaner.getType() == 1){
                ArrayList<NovelCleaner> tempArrCleaners = db.CreateCleanerConnectionsAny(cleaner);

                for(NovelCleaner nc : tempArrCleaners){
                    if(nc.equals(cleaner)){
                        cleaner = nc;
                        break;
                    }
                }

            }else if (cleaner.getType() == 2){
                ArrayList<NovelCleaner> tempArrCleaners = db.CreateCleanerConnectionsSource(cleaner, novelSource);

                for(NovelCleaner nc : tempArrCleaners){
                    if(nc.equals(cleaner)){
                        cleaner = nc;
                        break;
                    }
                }

            }else if(cleaner.getType() == 3){
                cleaner = db.CreateCleanerConnectionsNovel(cleaner, novelName, novelSource);
            }else {
                return null;
            }

            return cleaner;
        }

        @Override
        protected void onPostExecute(NovelCleaner cleaner) {
            super.onPostExecute(cleaner);

            if(cleaner != null){
                CreateCleaner(cleaner);
            }
        }
    }

    private class EditCleanerTask extends AsyncTask<NovelCleaner, Void, NovelCleaner> {
        @Override
        protected NovelCleaner doInBackground(NovelCleaner... novelCleaners) {
            DBController db = new DBController(ctx);
            NovelCleaner cleaner = novelCleaners[0];

            db.EditCleaner(cleaner);

            return cleaner;
        }

        @Override
        protected void onPostExecute(NovelCleaner cleaner) {
            super.onPostExecute(cleaner);

            if(cleaner != null){
                UpdateCleaner(cleaner);
            }
        }
    }
}
