package com.example.mobileproject.ui.source_top_fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobileproject.NovelsGridAdaptor;
import com.example.mobileproject.R;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.util.ArrayList;

public class TopFragment2 extends Fragment {
    Context currentContext = null;
    String sourceName;

    public TopFragment2(String sourceName) {
        this.sourceName = sourceName;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.visit_source_top_fragment_2, container, false);

        EditText text = (EditText) root.findViewById(R.id.novel_search_s);
        text.requestFocus();

        Button simpleButton1 = (Button) root.findViewById(R.id.return_fragment_s);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToDefault();
            }
        });


        Button simpleButton2 = (Button) root.findViewById(R.id.search_in_source);
        simpleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchValue = text.getText().toString();
                text.clearFocus();
                text.setInputType(InputType.TYPE_NULL);

                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                SearchNovelsFromThisSource s = new SearchNovelsFromThisSource();
                LinearLayout isLoading = (LinearLayout) requireActivity().findViewById(R.id.isLoading);

                if(isLoading.getChildCount() != 0){
                    return;
                }

                s.execute(searchValue);
            }
        });


        //novel_search_s
        return root;
    }

    private void returnToDefault() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.visit_source_top_fragments, new TopFragment1(sourceName));
        fragmentTransaction.commit(); // save the changes
    }


    private class SearchNovelsFromThisSource extends AsyncTask<String, Void, ArrayList<NovelDetailsMinimum>> {
        private LinearLayout isLoading;
        private GridView gridView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isLoading = (LinearLayout) requireActivity().findViewById(R.id.isLoading);
            isLoading.setVisibility(View.VISIBLE);

            View p = getLayoutInflater().inflate(R.layout.loading_animation, null);
            isLoading.addView(p);

            gridView = requireActivity().findViewById(R.id.novelsGrid);
        }

        @Override
        protected ArrayList<NovelDetailsMinimum> doInBackground(String... params) {
            ArrayList<NovelDetailsMinimum> novelDetailsArr;

            ParserInterface parser = ParserFactory.getParserInstance(sourceName, getContext());

            if(parser == null){
                return null;
            }

            System.out.println("Chegou aq");

            novelDetailsArr = parser.searchNovels(params[0]);

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<NovelDetailsMinimum> novelDetailsArr) {
            super.onPostExecute(novelDetailsArr);

            isLoading.setVisibility(View.INVISIBLE);
            isLoading.removeAllViews();

            if(novelDetailsArr == null) {
                return;
            }


            gridView.setAdapter(new NovelsGridAdaptor((Context) getActivity(), novelDetailsArr, sourceName));
        }
    }
}