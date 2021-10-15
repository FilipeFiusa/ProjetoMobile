package com.example.mobileproject.ui.downloads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.model.ChapterInDownload;

import java.util.ArrayList;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    private ArrayList<ChapterInDownload> mChapterList;

    public static class DownloadViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.d_novel_image);
            mTextView1 = itemView.findViewById(R.id.d_novel_name);
            mTextView2 = itemView.findViewById(R.id.d_chapter_name);
        }
    }

    public DownloadAdapter(ArrayList<ChapterInDownload> chapterList){
        this.mChapterList = chapterList;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false);

        DownloadViewHolder dvh = new DownloadViewHolder(v);

        return dvh;
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        ChapterInDownload currentItem = mChapterList.get(position);

        holder.mImageView.setImageBitmap(currentItem.getNovelImage());
        holder.mTextView1.setText(currentItem.getNovelName());
        holder.mTextView2.setText(currentItem.getChapterIndex().getChapterName());
    }

    @Override
    public int getItemCount() {
        return mChapterList.size();
    }
}
