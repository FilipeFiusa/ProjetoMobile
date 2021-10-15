package com.example.mobileproject.ui.downloads;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterInDownload;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.ui.library.LibraryNovelsGridAdaptor;

import java.util.ArrayList;

public class DownloadsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.download_list_fragment, container, false);

        this.root = root;

        DownloadList d = new DownloadList();
        d.execute();

        return root;
    }

    public class DownloadList extends AsyncTask<Void, Void, ArrayList<ChapterInDownload>> {
        private LinearLayout isLoading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //isLoading = (LinearLayout) findViewById(R.id.isLoading);
        }

        @Override
        protected ArrayList<ChapterInDownload> doInBackground(Void... voids) {
            ArrayList<ChapterInDownload>  novelDetailsArr;

            DBController db = new DBController(requireActivity().getApplicationContext());
            novelDetailsArr = db.getDownloadingChapters("Supreme Magus", "NovelFull");

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterInDownload>  novelDetails) {
            super.onPostExecute(novelDetails);

            mRecyclerView = root.findViewById(R.id.download_list);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(getContext());
            mAdapter = new DownloadAdapter(novelDetails);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }
    }
}