package com.example.mobileproject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcelable;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChaptersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private NovelDetails currentNovel;
    private ArrayList<NovelDetailsAdapterObject> mChapterList;
    private DownloadReceiver downloadReceiver;
    private AppCompatActivity ctx;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean exist = true;

    private String orderType = "ASC";

    private AddNovelOnFavorite addNovelOnFavorite = new AddNovelOnFavorite();

    private boolean isTextViewClicked = true;

    public static class ChapterViewHolder extends RecyclerView.ViewHolder{
        public ImageButton mImageButton;
        public TextView mTextView1;
        public View mButton;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageButton = itemView.findViewById(R.id.downloadButton);
            mTextView1 = itemView.findViewById(R.id.chapter_name);
            mButton = itemView;
        }
    }

    public static class NovelDetailsHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public TextView mTextView3;
        public TextView mTextView4;

        public Button mButton;
        public Button mButton2;
        public Button mButton3;

        public NovelDetailsHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.novel_image);
            mTextView1 = itemView.findViewById(R.id.novel_name);
            mTextView2 = itemView.findViewById(R.id.novel_author);
            mTextView3 = itemView.findViewById(R.id.novel_description);
            mTextView4 = itemView.findViewById(R.id.chapter_quantity);

            mButton = itemView.findViewById(R.id.add_favorite);//
            mButton2 = itemView.findViewById(R.id.sort_chapters);
            mButton3 = itemView.findViewById(R.id.visit_on_webview);
        }
    }

    public ChaptersAdapter() {
        this.exist = false;
    }

    public ChaptersAdapter(ArrayList<NovelDetailsAdapterObject> chapterList, AppCompatActivity ctx){
        this.mChapterList = chapterList;

        mChapterList.add(new NovelDetailsAdapterObject(new NovelDetails()));

        this.ctx = ctx;

        mSwipeRefreshLayout = (SwipeRefreshLayout) ctx.findViewById(R.id.swipeRefresh);

        ctx.findViewById(R.id.read_next_chapter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReadNextChapter();
            }
        });
    }

    public void addNovelDetails(NovelDetails n){
        mChapterList.set(0, new NovelDetailsAdapterObject(n));

        currentNovel = n;

        notifyItemChanged(0);
    }

    public void addChapterIndexes(ArrayList<ChapterIndex> c){
        for (int i = 0; i < c.size(); i++) {
            mChapterList.add(new NovelDetailsAdapterObject(c.get(i)));
        }
        NovelDetails n = mChapterList.get(0).getNovelDetails();
        n.setChapterIndexes(c);
        n.setChapterQuantity(c.size());

        sortList();

        notifyDataSetChanged();
    }

    public void addNewChapterIndexes(ArrayList<ChapterIndex> c) {
        String order = orderType;

        orderType = "ASC2";
        sortList();

        List<NovelDetailsAdapterObject> tempList = mChapterList.subList(1, mChapterList.size());

        for (int i = 0; i < c.size(); i++) {
            boolean updated = false;
            ChapterIndex currentItem = c.get(i);

            for(NovelDetailsAdapterObject o : tempList){
                ChapterIndex aux = o.getChapterIndex();
                if(currentItem.equals(aux)){
                    currentItem.updateSelf(aux);
                    updated = true;
                    System.out.println(currentItem.getId());
                    break;
                }
            }

            if(!updated){
                System.out.println(currentItem.getId());
                currentItem.setId(-1);
            }

            /*
            try {
                ChapterIndex current = mChapterList.get(i).getChapterIndex();
                ChapterIndex aux = c.get(i);

                if(current.getChapterLink().equals(aux.getChapterLink())
                        && current.getChapterName().equals(aux.getChapterName())
                ){
                    continue;
                }
            }catch (IndexOutOfBoundsException e){
                mChapterList.add(new NovelDetailsAdapterObject(c.get(i)));
            }

             */
        }

        if(order.equals("DSC") && orderType.equals("ASC2")){
            orderType = "DSC";
        }else if(orderType.equals("ASC2")){
            orderType = "ASC";
        }

        NovelDetailsAdapterObject o = mChapterList.remove(0);
        mChapterList.clear();
        mChapterList.add(o);

        UpdateChapterList updateChapterList = new UpdateChapterList();
        updateChapterList.execute(c);

    }

    public void sortList(){
        Collections.sort(mChapterList.subList(1, mChapterList.size()), new Comparator<NovelDetailsAdapterObject>() {
            @Override
            public int compare(NovelDetailsAdapterObject novelDetailsAdapterObject, NovelDetailsAdapterObject t1) {
                return Integer.compare(novelDetailsAdapterObject.getChapterIndex().getSourceId(), t1.getChapterIndex().getSourceId());
            }
        });
        if(orderType.equals("ASC2")){
            return;
        }

        if(orderType.equals("DSC")){
            Collections.reverse(mChapterList.subList(1, mChapterList.size()));
        }

        notifyItemRangeChanged(1, mChapterList.size());
    }

    public void updateChapterIndexes(ArrayList<ChapterIndex> c){
        NovelDetails n = mChapterList.get(0).getNovelDetails();
        n.setIsFavorite("yes");


        for (int i = 1; i < mChapterList.size(); i++) {
            mChapterList.set(i, new NovelDetailsAdapterObject(c.get(i-1)));
        }

        sortList();

        notifyDataSetChanged();
    }

    public void setDownloadReceiver(DownloadReceiver d){
        this.downloadReceiver = d;

        if(!isMyServiceRunning(DownloaderService.class)){
            return;
        }

        Intent serviceIntent = new Intent(ctx, DownloaderService.class);
        serviceIntent.putExtra("receiver", (Parcelable) downloadReceiver);
        ctx.startService(serviceIntent);
    }

    public void putChapterAsReadied(ArrayList<Integer> chaptersReadied){
        for (int i = 1; i < mChapterList.size(); i++) {
            ChapterIndex c = mChapterList.get(i).getChapterIndex();

            for (Integer id : chaptersReadied) {
                String chapter_id = String.valueOf(c.getId());
                String idReadied = String.valueOf(id);

                if(chapter_id.equals(idReadied)) {
                    c.setReaded("yes");
                    break;
                }
            }
        }
        notifyDataSetChanged();
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
            if(currentItem.getReaded().equals("yes")){
                chapterViewHolder.mTextView1.setTextColor(Color.GRAY);
            }else {
                chapterViewHolder.mTextView1.setTextColor(Color.WHITE);
            }

            if(currentItem.getDownloaded().equals("no")){
                chapterViewHolder.mImageButton.setImageResource(R.drawable.ic_outline_arrow_circle_down_40);
                chapterViewHolder.mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!currentNovel.getIsFavorite().equals("yes")){
                            Toast.makeText(ctx, "Adicione Novel no Favoritos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        currentItem.setDownloaded("downloading");
                        notifyItemChanged(chapterViewHolder.getAdapterPosition());
                        PutChapterOnDownloadList p = new PutChapterOnDownloadList(chapterViewHolder.mImageButton);
                        p.execute(currentItem.getId());
                    }
                });

            }else if(currentItem.getDownloaded().equals("downloading")){
                chapterViewHolder.mImageButton.setImageResource(R.drawable.ic_outline_cancel_40);
            }else{
                chapterViewHolder.mImageButton.setImageResource(R.drawable.ic_round_check_circle_40);
            }

            chapterViewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ADD your action here
                    ChapterIndex currentItem = mChapterList.get(chapterViewHolder.getAdapterPosition()).getChapterIndex();
                    Intent intent = new Intent(ctx, ReaderActivity.class);
                    intent.putExtra("NovelReaderController", new NovelReaderController(currentNovel.getChapterIndexes()));
                    intent.putExtra("chapterLink", currentItem.getChapterLink());
                    intent.putExtra("novelName", currentNovel.getNovelName());
                    intent.putExtra("sourceName", currentNovel.getSource());
                    ctx.startActivityForResult(intent, 1);
                }
            });

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
        if(currentNovel.getNovelImage() != null){
            novelDetailsHolder.mImageView.setImageBitmap(currentNovel.getNovelImage());
        }

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

        novelDetailsHolder.mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, VisitSourceWebViewActivity.class);
                intent.putExtra("SourceName", currentNovel.getSource());
                intent.putExtra("NovelLink", currentNovel.getNovelLink());
                ctx.startActivity(intent);
            }
        });

        novelDetailsHolder.mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mChapterList.size() > 1){
                    sortList();
                    System.out.println(orderType);
                    if(orderType.equals("ASC")){
                        orderType = "DSC";
                    }else{
                        orderType = "ASC";
                    }
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

        mSwipeRefreshLayout.setRefreshing(true);

        addNovelOnFavorite = new AddNovelOnFavorite();
        addNovelOnFavorite.execute();
    }

    private void unsetFavorite() {
        return;
    }

    private void ReadNextChapter(){
        for (int i = 1; i < mChapterList.size(); i++) {
            ChapterIndex currentItem = mChapterList.get(i).getChapterIndex();

            if(currentItem.getReaded().equals("no")){
                Intent intent = new Intent(ctx, ReaderActivity.class);
                intent.putExtra("NovelReaderController", new NovelReaderController(currentNovel.getChapterIndexes()));
                intent.putExtra("chapterLink", currentItem.getChapterLink());
                intent.putExtra("novelName", currentNovel.getNovelName());
                ctx.startActivityForResult(intent, 1);

                return;
            }
        }
    }

    public void ChapterDownloaded(int id){
        if(!exist){
            return;
        }

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
                serviceIntent.putExtra("receiver", (Parcelable) downloadReceiver);
                ctx.startService(serviceIntent);
            }
        }
    }

    private class AddNovelOnFavorite extends AsyncTask<Void, Void, ArrayList<ChapterIndex>> {

        @Override
        protected ArrayList<ChapterIndex> doInBackground(Void... params) {
            DBController db = new DBController(ctx);
            db.putOnLibrary(currentNovel.getNovelName(), currentNovel.getSource());

            return db.getChaptersFromANovel(currentNovel.getNovelName(),
                    currentNovel.getSource());
        }

        @Override
        protected void onPostExecute(ArrayList<ChapterIndex> newChapterIndexes) {
            super.onPostExecute(newChapterIndexes);


            currentNovel.setIsFavorite("yes");
            notifyItemChanged(0);
            updateChapterIndexes(newChapterIndexes);

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateChapterList extends  AsyncTask<ArrayList<ChapterIndex>, Void, Void>{
        @Override
        protected Void doInBackground(ArrayList<ChapterIndex>... arrayLists) {
            DBController db = new DBController(ctx);
            db.updateChapters(currentNovel.getNovelName(), currentNovel.getSource(), arrayLists[0]);

            mSwipeRefreshLayout.setRefreshing(false);
            addChapterIndexes(arrayLists[0]);

            return null;
        }
    }
}
