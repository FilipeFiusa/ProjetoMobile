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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.ui.top_fragment.TopFragment1;

import java.util.ArrayList;
import java.util.List;

public class NavigateFragment extends Fragment {
    Context currentContext = null;

    private RecyclerView mRecycleView;
    private SourceContainerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigate_fragment, container, false);
        

        List<Parser> parsers = ParserFactory.getAllParsersWithPreferences(getContext());

        mRecycleView = (RecyclerView) root.findViewById(R.id.source_container_recycle_view);

        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new SourceContainerAdapter(getContext(), parsers);

        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(mLayoutManager);

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
