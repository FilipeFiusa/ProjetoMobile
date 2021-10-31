package com.example.mobileproject.ui.library;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsAdapterObject;

import java.util.ArrayList;

public class NovelsAdapter extends RecyclerView.Adapter<NovelsAdapter.NovelDetailsViewHolder> {

    private ArrayList<NovelDetails> mNovelList;
    private DownloadReceiver downloadReceiver;
    private AppCompatActivity ctx;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean exist = true;

    private String orderType = "DSC";

    private boolean isTextViewClicked = true;

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

    public NovelsAdapter(ArrayList<NovelDetails> chapterList, AppCompatActivity ctx){
        this.mNovelList = chapterList;

        this.ctx = ctx;

        mSwipeRefreshLayout = (SwipeRefreshLayout) ctx.findViewById(R.id.swipeRefresh);
    }

    public void updateNovelsList(ArrayList<NovelDetails> chapterList){
        this.mNovelList = new ArrayList<>();

        this.mNovelList = chapterList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NovelDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.novel_grid_button, parent, false);;
        return new NovelDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelDetailsViewHolder holder, int position) {
        NovelDetails currentItem = mNovelList.get(position);

        holder.mTextView1.setText(currentItem.getNovelName());
        holder.mImageButton.setImageBitmap(currentItem.getNovelImage());
        holder.mTextView2.setText(String.valueOf(currentItem.getChapterToReadQuantity()));

        holder.mImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(ctx, NovelDetailsActivity.class);

                intent.putExtra("NovelDetails_name", currentItem.getNovelName());
                intent.putExtra("isFavorite", "yes");
                intent.putExtra("NovelDetails_source", currentItem.getSource());

                ctx.startActivity(intent);
                ctx.startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mNovelList.size();
    }
}
