package com.example.mobileproject;

import android.app.ActivityManager;
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
import android.widget.Button;
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
import com.example.mobileproject.model.ChapterAdapterReference;
import com.example.mobileproject.model.ChapterInDownload;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsAdapterObject;
import com.example.mobileproject.model.NovelReaderController;

import java.util.ArrayList;

public class ChaptersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private NovelDetails currentNovel;
    private ArrayList<NovelDetailsAdapterObject> mChapterList;
    private DownloadReceiver downloadReceiver;
    private Context ctx;

    private AddNovelOnFavorite addNovelOnFavorite = new AddNovelOnFavorite();

    private boolean isTextViewClicked = true;

    public static class ChapterViewHolder extends RecyclerView.ViewHolder{
        public ImageButton mImageButton;
        public TextView mTextView1;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageButton = itemView.findViewById(R.id.downloadButton);
            mTextView1 = itemView.findViewById(R.id.chapter_name);
        }
    }

    public static class NovelDetailsHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public TextView mTextView3;
        public TextView mTextView4;

        public Button mButton;

        public NovelDetailsHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.novel_image);
            mTextView1 = itemView.findViewById(R.id.novel_name);
            mTextView2 = itemView.findViewById(R.id.novel_author);
            mTextView3 = itemView.findViewById(R.id.novel_description);
            mTextView4 = itemView.findViewById(R.id.chapter_quantity);

            mButton = itemView.findViewById(R.id.add_favorite);
        }
    }

    public ChaptersAdapter(ArrayList<NovelDetailsAdapterObject> chapterList, Context ctx){
        this.mChapterList = chapterList;
        this.ctx = ctx;
    }

    public void addNovelDetails(NovelDetails n){
        mChapterList.add(new NovelDetailsAdapterObject(n));
        currentNovel = n;
        notifyDataSetChanged();
    }

    public void addChapterIndexes(ArrayList<ChapterIndex> c){
        for (int i = 0; i < c.size(); i++) {
            mChapterList.add(new NovelDetailsAdapterObject(c.get(i)));
        }
        NovelDetails n = mChapterList.get(0).getNovelDetails();
        n.setChapterIndexes(c);
        n.setChapterQuantity(c.size());

        notifyDataSetChanged();
    }

    public void setDownloadReceiver(DownloadReceiver d){
        this.downloadReceiver = d;

        if(!isMyServiceRunning(DownloaderService.class)){
            return;
        }

        Intent serviceIntent = new Intent(ctx, DownloaderService.class);
        serviceIntent.putExtra("receiver", downloadReceiver);
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (viewType == 0) {
            view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_grid_button, parent, false);;
            return new ChapterViewHolder(view);
        } else if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.novel_details_header, parent, false);
            return new NovelDetailsHolder(view);
        }else {
            return  null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final int itemType = getItemViewType(position);
        // First check here the View Type
        // than set data based on View Type to your recyclerview item
        if (itemType == 0) {
            ChapterViewHolder chapterViewHolder = (ChapterViewHolder) holder;

            ChapterIndex currentItem = mChapterList.get(position).getChapterIndex();

            chapterViewHolder.mTextView1.setText(currentItem.getChapterName());

            if(currentItem.getDownloaded().equals("no")){
                chapterViewHolder.mImageButton.setImageResource(R.drawable.ic_outline_arrow_circle_down_40);
                chapterViewHolder.mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PutChapterOnDownloadList p = new PutChapterOnDownloadList(chapterViewHolder.mImageButton);
                        p.execute(currentItem.getId());
                    }
                });

            }else if(currentItem.getDownloaded().equals("downloading")){
                chapterViewHolder.mImageButton.setImageResource(R.drawable.ic_outline_cancel_40);
            }else{
                chapterViewHolder.mImageButton.setImageResource(R.drawable.ic_round_check_circle_40);
            }

        } else if (itemType == 1) {
            NovelDetailsHolder novelDetailsHolder = (NovelDetailsHolder) holder;
            NovelDetails currentNovel = mChapterList.get(position).getNovelDetails();

            SetUpNovelDetailsHeader(novelDetailsHolder, currentNovel);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // based on you list you will return the ViewType
        return mChapterList.get(position).getViewType();
    }

    private void SetUpNovelDetailsHeader(NovelDetailsHolder novelDetailsHolder, NovelDetails currentNovel){
        novelDetailsHolder.mImageView.setImageBitmap(currentNovel.getNovelImage());
        novelDetailsHolder.mTextView1.setText(currentNovel.getNovelName());
        novelDetailsHolder.mTextView2.setText(currentNovel.getNovelAuthor());
        novelDetailsHolder.mTextView3.setText(currentNovel.getNovelDescription());
        novelDetailsHolder.mTextView4.setText(new StringBuilder().append(currentNovel.getChapterQuantity()).append(" Capitulos").toString());

        SetUpFavoriteButtons(novelDetailsHolder, currentNovel.getIsFavorite());

        novelDetailsHolder.mTextView3.setMaxLines(3);
        novelDetailsHolder.mTextView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    novelDetailsHolder.mTextView3.setMaxLines(3);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    novelDetailsHolder.mTextView3.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
            }
        });
    }

    private void SetUpFavoriteButtons(NovelDetailsHolder holder, String isFavorite){
        if(isFavorite == null || isFavorite.equals("no")){
            holder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setFavorite();
                }
            });
            return;
        }

        holder.mButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_favorite_64, 0,0);

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //unsetFavorite();
            }
        });

    }

    private void setFavorite(){
        if(currentNovel == null || addNovelOnFavorite.getStatus() == AsyncTask.Status.RUNNING){
            return;
        }


        currentNovel.setIsFavorite("yes");
        notifyItemChanged(0);


        addNovelOnFavorite = new AddNovelOnFavorite();
        addNovelOnFavorite.execute();
    }

    private void unsetFavorite() {
        return;
    }

    /*
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
    }*/

    /*
    public void updateList(ArrayList<ChapterIndex> list, NovelDetails novelDetails){
        this.mChapterList = list;
        this.currentNovel = novelDetails;
    }
    */

    public void ChapterDownloaded(int id){
        Log.d("------ ", "ChapterDownloaded - Id: " + id);

        for (int i = 1; i < mChapterList.size(); i++) {
            ChapterIndex c = mChapterList.get(i).getChapterIndex();

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
            currentNovel = mChapterList.get(0).getNovelDetails();

            return db.putChapterOnDownload(currentNovel.getNovelName(),
                    currentNovel.getSource(), integers[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                imageButton.setImageResource(R.drawable.ic_outline_cancel_40);

                Intent serviceIntent = new Intent(ctx, DownloaderService.class);
                serviceIntent.putExtra("NovelName", currentNovel.getNovelName());
                serviceIntent.putExtra("Source", currentNovel.getSource());
                serviceIntent.putExtra("receiver", downloadReceiver);
                ctx.startService(serviceIntent);
            }
        }
    }

    private class AddNovelOnFavorite extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DBController db = new DBController(ctx);
            db.insertNovel(currentNovel.getNovelName(), currentNovel.getNovelAuthor(), currentNovel.getNovelDescription(), "NovelFull",  currentNovel.getNovelImage());
            ArrayList<ChapterIndex> chapterIndices = currentNovel.getChapterIndexes();
            for(ChapterIndex c : chapterIndices){
                db.insertChapters(currentNovel.getNovelName(), "NovelFull", c);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

}
