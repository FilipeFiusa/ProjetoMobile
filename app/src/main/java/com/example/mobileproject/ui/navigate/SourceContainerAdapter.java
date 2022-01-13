package com.example.mobileproject.ui.navigate;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.ChaptersAdapter;
import com.example.mobileproject.R;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.Languages;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.ui.library.NovelsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceContainerAdapter extends RecyclerView.Adapter<SourceContainerAdapter.SourceContainerViewHolder> {

    private final Context ctx;
    private final List<Parser> mParsers;
    private final ArrayList<Languages> mLanguages = new ArrayList<>(Arrays.asList(Languages.values()));

    private SourceItemAdapter fixedAdapter;
    private int pinnedCount = 0;

    public SourceContainerAdapter(Context ctx, List<Parser> mParsers) {
        this.ctx = ctx;
        this.mParsers = mParsers;

        for(Parser parser : mParsers){
            if (parser.isPinned){
                pinnedCount += 1;
            }
        }

        if(pinnedCount == 0){
            mLanguages.remove(0);
        }
    }

    @NonNull
    @Override
    public SourceContainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.navigate_source_container, parent, false);;
        return new SourceContainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SourceContainerViewHolder holder, int position) {
        Languages currentItem = mLanguages.get(position);
        ArrayList<Parser> tempList = new ArrayList<>();

        if(currentItem == Languages.FIXED){
            for(Parser parser : mParsers){
                if (parser.isPinned){
                    tempList.add(parser);
                }
            }
        }else{
            for(Parser parser : mParsers){
                if(parser.getLanguage() == currentItem){
                    tempList.add(parser);
                }
            }
        }

        if(tempList.isEmpty()){
            holder.itemView.setVisibility(View.GONE);
            return;
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ctx);

        SourceItemAdapter mAdapter;
        if(currentItem == Languages.FIXED){
            mAdapter = new SourceItemAdapter(ctx, tempList, this, true);
            fixedAdapter = mAdapter;
        }else{
            mAdapter = new SourceItemAdapter(ctx, tempList, this);
        }

        holder.mTextView.setText(currentItem.getFullName());

        holder.mNestedRecycleView.setAdapter(mAdapter);
        holder.mNestedRecycleView.setLayoutManager(mLayoutManager);

        if(currentItem == Languages.FIXED){
            fixedAdapter = mAdapter;
        }
    }

    public void sourceItemFixed(Parser parser){
        if(pinnedCount == 0){
            mLanguages.add(0, Languages.FIXED);
            notifyItemInserted(0);
            pinnedCount++;
            return;
        }

        //notifyItemChanged(0);
        fixedAdapter.insertItem(parser);
        pinnedCount++;
    }

    public void sourceItemUnFixed(Parser parser){
        pinnedCount -= 1;

        if(pinnedCount == 0){
            mLanguages.remove(0);
            notifyItemRemoved(0);
        }

        //notifyItemChanged(0);
        fixedAdapter.removeItem(parser);
    }

    @Override
    public int getItemCount() {
        return mLanguages.size();
    }

    public static class SourceContainerViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;
        public RecyclerView mNestedRecycleView;

        public SourceContainerViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.container_language);
            mNestedRecycleView = itemView.findViewById(R.id.source_item_recycle_view);
        }
    }
}
