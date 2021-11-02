package com.example.mobileproject.ui.source_top_fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobileproject.R;

public class TopFragment1 extends Fragment {
    Context currentContext = null;
    String sourceName = "";

    public TopFragment1(String sourceName) {
        this.sourceName = sourceName;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.visit_source_top_fragment_1, container, false);

        Button simpleButton1 = (Button) root.findViewById(R.id.return_activity_s);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        TextView textView = (TextView) root.findViewById(R.id.source_name);
        textView.setText(sourceName);

        Button simpleButton3 = (Button) root.findViewById(R.id.go_to_search_in_source);
        simpleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSearch();
            }
        });

        return root;
    }

    private void goToSearch() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.visit_source_top_fragments, new TopFragment2(sourceName));
        fragmentTransaction.commit(); // save the changes
    }


}