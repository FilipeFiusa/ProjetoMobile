package com.example.mobileproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.ReaderMenuItem;

import java.util.ArrayList;
import java.util.logging.Handler;

public class ReaderChaptersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ReaderActivity readerActivity;

    private ArrayList<ReaderMenuItem> mList = new ArrayList<>();
    private NovelReaderController nrc;

    private boolean exist = true;

    private Context ctx;

    private DownloadReceiver downloadReceiver;

    public int currentReadingPosition;

    public static class ReaderChaptersViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;
        public TextView mTextView;
        public View mButton;

        public ReaderChaptersViewHolder(@NonNull View itemView) {
            super(itemView);
            mButton = itemView;
            mImageView = itemView.findViewById(R.id.is_downloaded);
            mTextView = itemView.findViewById(R.id.reader_menu_chapter_name);
        }
    }

    public ReaderChaptersAdapter() {
        this.exist = false;
    }

    public ReaderChaptersAdapter(NovelReaderController nrc, String chapterLink, ReaderActivity readerActivity) {
        this.nrc = nrc;
        this.readerActivity = readerActivity;
        this.ctx = readerActivity;

        ArrayList<ChapterIndex> mList = nrc.getChapterIndices();
        int i = 0;

        for (ChapterIndex c : mList){
            if(c.getChapterLink().equals(chapterLink)){
                this.mList.add(new ReaderMenuItem(c, true));
                currentReadingPosition = i;
            }else {
                this.mList.add(new ReaderMenuItem(c, false));
            }
            i++;
        }
    }

    public void setDownloadReceiver(DownloadReceiver d){
        this.downloadReceiver = d;

        if(!isMyServiceRunning(DownloaderService.class)){
            return;
        }

        Intent serviceIntent = new Intent(ctx, DownloaderService.class);
        serviceIntent.putExtra("receiver2", (Parcelable) downloadReceiver);
        ctx.startService(serviceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void ChapterDownloaded(int id){
        if(!exist){
            return;
        }

        Log.d("------ ", "ChapterDownloaded - Id: " + id);

        for (int i = 1; i < mList.size(); i++) {
            ChapterIndex c = mList.get(i).getChapterIndex();

            if(id == c.getId()){
                c.setDownloaded("yes");
                Log.d("------ ", "ID achado na lista");
                notifyItemChanged(i);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.reader_menu_button_item, parent, false);;
        return new ReaderChaptersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReaderChaptersViewHolder mHolder = (ReaderChaptersViewHolder) holder;
        ReaderMenuItem item = mList.get(mHolder.getAdapterPosition());

        mHolder.mTextView.setText(item.getChapterIndex().getChapterName());
        Log.i(String.valueOf(item.getIsSelected()), item.getChapterIndex().getChapterLink());
        if (!item.getIsSelected()){
            mHolder.mTextView.setTextColor(Color.WHITE);
        }else {
            currentReadingPosition = mHolder.getAdapterPosition();
            mHolder.mTextView.setTextColor(Color.YELLOW);
        }

        if (item.getChapterIndex().getDownloaded().equals("yes")){
            mHolder.mImageView.setVisibility(View.VISIBLE);
        }else{
            mHolder.mImageView.setVisibility(View.GONE);
        }

        mHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChapterIndex c = nrc.goToChapter(mHolder.getAdapterPosition());
                readerActivity.goTo(c);

                mList.get(currentReadingPosition).setIsSelected(false);
                notifyItemChanged(currentReadingPosition);
                mList.get(mHolder.getAdapterPosition()).setIsSelected(true);
                currentReadingPosition = mHolder.getAdapterPosition();
                notifyItemChanged(currentReadingPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
