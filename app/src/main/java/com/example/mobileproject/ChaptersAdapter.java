package com.example.mobileproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.ChapterInDownload;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelReaderController;

import java.util.ArrayList;

public class ChaptersAdapter extends RecyclerView.Adapter<ChaptersAdapter.ChapterViewHolder> {

    private NovelDetails currentNovel;
    private ArrayList<ChapterIndex> mChapterList;
    private Context ctx;

    public static class ChapterViewHolder extends RecyclerView.ViewHolder{
        public ImageButton mImageButton;
        public TextView mTextView1;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageButton = itemView.findViewById(R.id.downloadButton);
            mTextView1 = itemView.findViewById(R.id.chapter_name);
        }
    }

    public ChaptersAdapter(ArrayList<ChapterIndex> chapterList, Context ctx){
        this.mChapterList = chapterList;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_grid_button, parent, false);

        ChapterViewHolder dvh = new ChapterViewHolder(v);

        return dvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        ChapterIndex currentItem = mChapterList.get(position);

        holder.mTextView1.setText(currentItem.getChapterName());

        if(currentItem.getDownloaded().equals("no")){
            holder.mImageButton.setImageResource(R.drawable.ic_outline_arrow_circle_down_40);
        }else if(currentItem.getDownloaded().equals("downloading")){
            holder.mImageButton.setImageResource(R.drawable.ic_outline_cancel_40);
        }else{
            holder.mImageButton.setImageResource(R.drawable.ic_round_check_circle_40);
        }

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ADD your action here
                Intent intent = new Intent(ctx, ReaderActivity.class);
                intent.putExtra("NovelReaderController", new NovelReaderController(mChapterList));
                intent.putExtra("chapterLink", currentItem.getChapterLink());
                ctx.startActivity(intent);
            }
        });

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentItem.getDownloaded().equals("downloading") || currentItem.getDownloaded().equals("yes") ){
                    return;
                }
                Log.i("Clicou em ", String.valueOf(currentItem.getId()));

                PutChapterOnDownloadList p = new PutChapterOnDownloadList(holder.mImageButton);
                p.execute(currentItem.getId());
            }
        });
    }

    public void updateList(ArrayList<ChapterIndex> list, NovelDetails novelDetails){
        this.mChapterList = list;
        this.currentNovel = novelDetails;
    }

    public void ChapterDownloaded(int id){
        Log.d("------ ", "ChapterDownloaded - Id: " + id);

        for (int i = 0; i < mChapterList.size(); i++) {
            ChapterIndex c = mChapterList.get(i);

            if(id == c.getId()){
                c.setDownloaded("yes");
                Log.d("------ ", "ID achado na lista");
                notifyItemChanged(i);
            }
        }
    }



    @Override
    public int getItemCount() {
        return mChapterList.size();
    }

    private class PutChapterOnDownloadList extends AsyncTask<Integer, Void, Boolean> {

        ImageButton imageButton;

        public PutChapterOnDownloadList(ImageButton imageButton){
            this.imageButton = imageButton;
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            DBController db = new DBController(ctx);

            return db.putChapterOnDownload(currentNovel.getNovelName(),
                    currentNovel.getSource(), integers[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                imageButton.setImageResource(R.drawable.ic_outline_cancel_40);
            }
        }
    }

}
