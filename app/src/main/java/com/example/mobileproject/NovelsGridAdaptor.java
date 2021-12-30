package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;

import java.util.ArrayList;

public class NovelsGridAdaptor extends RecyclerView.Adapter<NovelsGridAdaptor.NovelDetailsViewHolder> {
    private ArrayList<NovelDetailsMinimum> mNovelList;
    private String novelSource;
    private AppCompatActivity ctx;


    public static class NovelDetailsViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageButton;
        public TextView mTextView1;
        public TextView mTextView2;

        public NovelDetailsViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageButton = itemView.findViewById(R.id.grid_novel_image);
            mTextView1 = itemView.findViewById(R.id.grid_novel_name);
            mTextView2 = itemView.findViewById(R.id.grid_chapter_count);
        }
    }

    public NovelsGridAdaptor(ArrayList<NovelDetailsMinimum> chapterList, AppCompatActivity ctx, String novelSource){
        this.mNovelList = chapterList;

        this.ctx = ctx;

        this.novelSource = novelSource;
    }

    public void updateNovelsList(ArrayList<NovelDetailsMinimum> chapterList){
        this.mNovelList = chapterList;
        notifyDataSetChanged();
    }

    public void updateSpecificItem(NovelDetailsMinimum updatedItem){
        for(int i = 0; i < mNovelList.size(); i++){
            NovelDetailsMinimum current = mNovelList.get(i);

            if(current.getId() == updatedItem.getId()){
                current.setNovelImage(updatedItem.getNovelImage());
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public NovelsGridAdaptor.NovelDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.novel_grid_button, parent, false);;
        return new NovelsGridAdaptor.NovelDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelDetailsViewHolder holder, int position) {
        NovelDetailsMinimum currentItem = mNovelList.get(position);

        holder.mTextView1.setText(currentItem.getNovelName());
        holder.mImageButton.setImageBitmap(currentItem.getNovelImage());
        holder.mTextView2.setVisibility(View.GONE);

        holder.mImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(ctx, NovelDetailsActivity.class);
                intent.putExtra("novelLink", currentItem.getNovelLink());
                intent.putExtra("novelName", currentItem.getNovelName());
                intent.putExtra("novelSource", novelSource);
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNovelList.size();
    }
}