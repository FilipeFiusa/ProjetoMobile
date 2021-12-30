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
import com.example.mobileproject.VisitSourceActivity;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.ParserInterface;
import com.example.mobileproject.model.parser.english.NovelFullParser;

import java.util.ArrayList;

public class TopFragment2 extends Fragment {
    VisitSourceActivity mainActivity = null;
    String sourceName;

    public TopFragment2(String sourceName, VisitSourceActivity mainActivity) {
        this.sourceName = sourceName;
        this.mainActivity = mainActivity;
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

                mainActivity.SearchNovel(searchValue);

/*                SearchNovelsFromThisSource s = new SearchNovelsFromThisSource();
                //LinearLayout isLoading = (LinearLayout) requireActivity().findViewById(R.id.isLoading);

*//*                if(isLoading.getChildCount() != 0){
                    return;
                }*//*

                s.execute(searchValue);*/
            }
        });


        //novel_search_s
        return root;
    }

    private void returnToDefault() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.visit_source_top_fragments, new TopFragment1(sourceName, mainActivity));
        fragmentTransaction.commit(); // save the changes
    }
}