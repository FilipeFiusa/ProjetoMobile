package com.example.mobileproject.ui.navigate;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobileproject.R;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.ui.top_fragment.TopFragment1;

public class NavigateFragment extends Fragment {
    Context currentContext = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigate_fragment, container, false);


        Parser[] lista = ParserFactory.getAllParsers(currentContext).toArray(new Parser[0]);
        GridView gv = (GridView) root.findViewById(R.id.fontsGrid);
        gv.setAdapter(new FontsGridAdaptor(currentContext, lista));

        loadFragment(new TopFragment1());

        return root;
    }

    public NavigateFragment(Context ctx){
        currentContext = ctx;
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.top_fragments, fragment);
        fragmentTransaction.commit(); // save the changes
    }
}
