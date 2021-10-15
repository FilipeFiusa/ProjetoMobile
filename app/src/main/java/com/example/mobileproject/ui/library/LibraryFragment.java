package com.example.mobileproject.ui.library;

import android.annotation.SuppressLint;
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

import com.example.mobileproject.R;
import com.example.mobileproject.VisitSourceActivity;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.NovelDetails;

import java.util.ArrayList;

public class LibraryFragment extends Fragment {
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.library_fragment, container, false);

        NovelsOnLibrary novelsOnLibrary = new NovelsOnLibrary();
        novelsOnLibrary.execute();

        this.root = root;

        return root;
    }

    public class NovelsOnLibrary extends AsyncTask<Void, Void, ArrayList<NovelDetails>> {
        private LinearLayout isLoading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //isLoading = (LinearLayout) findViewById(R.id.isLoading);
        }

        @Override
        protected ArrayList<NovelDetails> doInBackground(Void... voids) {
            ArrayList<NovelDetails> novelDetailsArr;

            DBController db = new DBController(requireActivity().getApplicationContext());
            novelDetailsArr = db.selectAllNovels();

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<NovelDetails> novelDetailsArr) {
            super.onPostExecute(novelDetailsArr);
            //isLoading.setVisibility(View.INVISIBLE);
            //isLoading.removeAllViews();

            GridView gv = (GridView) root.findViewById(R.id.novelsGrid);
            gv.setAdapter(new LibraryNovelsGridAdaptor((Context) root.getContext(), novelDetailsArr));
        }
    }

}