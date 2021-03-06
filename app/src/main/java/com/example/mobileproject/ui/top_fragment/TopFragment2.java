package com.example.mobileproject.ui.top_fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

        ImageButton simpleButton1 = (ImageButton) root.findViewById(R.id.return_button);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToDefault();
            }
        });

        EditText text = (EditText) root.findViewById(R.id.search_text);
        text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    TextView textView = (TextView) requireActivity().findViewById(R.id.search_text);
                    String search_text = textView.getText().toString();

                    if(search_text.length() == 0){
                        Toast.makeText(requireActivity(), "Digite alguma coisa", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    Intent intent = new Intent(getActivity(), VisitAllSourcesActivity.class);
                    intent.putExtra("search_text", search_text);
                    requireActivity().startActivity(intent);

                    return true;
                }
                return false;
            }
        });

        ImageButton simpleButton2 = (ImageButton) root.findViewById(R.id.searchSomething);
        simpleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) requireActivity().findViewById(R.id.search_text);
                String search_text = textView.getText().toString();

                if(search_text.length() == 0){
                    Toast.makeText(requireActivity(), "Digite alguma coisa", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Intent intent = new Intent(getActivity(), VisitAllSourcesActivity.class);
                intent.putExtra("search_text", search_text);
                requireActivity().startActivity(intent);
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