package com.example.mobileproject.ui.top_fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobileproject.R;
import com.example.mobileproject.VisitAllSourcesActivity;

public class TopFragment2 extends Fragment {
    Context currentContext = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigate_top_fragment_2, container, false);
//return_button

        Button simpleButton1 = (Button) root.findViewById(R.id.return_button);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToDefault();
            }
        });

        Button simpleButton2 = (Button) root.findViewById(R.id.searchSomething);
        simpleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(new Intent(getActivity(), VisitAllSourcesActivity.class));
            }
        });

        return root;
    }

    private void returnToDefault() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.top_fragments, new TopFragment1());
        fragmentTransaction.commit(); // save the changes
    }



}