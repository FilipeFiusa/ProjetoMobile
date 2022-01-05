package com.example.mobileproject.ui.library;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsAdapterObject;

import java.util.ArrayList;

public class NovelsAdapter extends RecyclerView.Adapter<NovelsAdapter.NovelDetailsViewHolder> {

    private ArrayList<NovelDetails> mNovelList;
    private AppCompatActivity ctx;
    private LibraryFragment libraryFragment;

    private ArrayList<NovelDetails> selectedNovels = new ArrayList<>();


    public static class NovelDetailsViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout mContainer;
        public ImageView mImageButton;
        public TextView mTextView1;
        public TextView mTextView2;

        public NovelDetailsViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageButton = itemView.findViewById(R.id.grid_novel_image);
            mTextView1 = itemView.findViewById(R.id.grid_novel_name);
            mTextView2 = itemView.findViewById(R.id.grid_chapter_count);
            mContainer = itemView.findViewById(R.id.novel_grid_item_container);
        }
    }

    public NovelsAdapter(ArrayList<NovelDetails> chapterList, AppCompatActivity ctx, LibraryFragment libraryFragment){
        this.mNovelList = chapterList;
        this.ctx = ctx;
        this.libraryFragment = libraryFragment;
    }

    public void updateNovelsList(ArrayList<NovelDetails> chapterList){
        this.mNovelList = new ArrayList<>();

        this.mNovelList = chapterList;
        notifyDataSetChanged();
    }

    public void resetSelectedList(){
        for(NovelDetails n : selectedNovels){
            n.selected = false;
            notifyItemChanged(n.position);
        }

        selectedNovels = new ArrayList<>();
    }

    public void selectAll(){
        if(mNovelList.size() == selectedNovels.size()) return;

        for(int i = 0; i < mNovelList.size(); i++){
            NovelDetails c = mNovelList.get(i);

            if(!c.selected){
                c.selected = true;
                selectedNovels.add(c);
                notifyItemChanged(i);
            }
        }
    }

    public void invertSelection(){
        for (int i = 0; i < mNovelList.size(); i++) {
            NovelDetails c = mNovelList.get(i);

            if(selectedNovels.contains(c)){
                c.selected = false;
                selectedNovels.remove(c);
                notifyItemChanged(i);
                continue;
            }

            c.selected = true;
            selectedNovels.add(c);
            notifyItemChanged(i);
        }

        libraryFragment.update();
    }

    public void removeSelected(){
        if(selectedNovels.size() == 1){
            NovelDetails removed = selectedNovels.remove(0);
            mNovelList.remove(removed);
            notifyItemRemoved(removed.position);
            resetSelectedList();

            return;
        }

        mNovelList.removeAll(selectedNovels);
        notifyItemRangeRemoved(0, mNovelList.size());
        notifyItemRangeChanged(0, mNovelList.size());
        resetSelectedList();
    }

    public void setAsReadied(){
        for(NovelDetails n : selectedNovels){
            n.setChapterToReadQuantity(0);
            notifyItemChanged(n.position);
        }

        resetSelectedList();
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

        currentItem.position = holder.getAdapterPosition();
        holder.mTextView1.setText(currentItem.getNovelName());
        holder.mImageButton.setImageBitmap(currentItem.getNovelImage());
        if(currentItem.getChapterToReadQuantity() == 0){
            holder.mTextView2.setVisibility(View.GONE);
        }else{
            holder.mTextView2.setText(String.valueOf(currentItem.getChapterToReadQuantity()));
        }

        if(currentItem.selected){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mContainer.setForeground(ContextCompat.getDrawable(ctx, R.drawable.border));
                holder.mImageButton.setForeground(ContextCompat.getDrawable(ctx, R.drawable.selectable_item_background));
            }else {
                holder.mContainer.setBackground(ContextCompat.getDrawable(ctx, R.drawable.border));
            }
            holder.mTextView1.setBackgroundColor(ctx.getResources().getColor(R.color.transparent));
            holder.mImageButton.setBackgroundColor(ctx.getResources().getColor(R.color.transparent));
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mContainer.setForeground(null);
                holder.mImageButton.setForeground(null);
            }else {
                holder.mContainer.setBackground(null);
            }

            holder.mTextView1.setBackground(ContextCompat.getDrawable(ctx, R.drawable.gradient));
            holder.mImageButton.setBackground(ContextCompat.getDrawable(ctx, R.drawable.selectable_item_background));
        }

        holder.mImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                NovelDetails currentItem = mNovelList.get(holder.getAdapterPosition());
                int highestValue = -1, lowestValue = -1;

                if(currentItem.selected){
                    return false;
                }

                if(selectedNovels.size() == 0){
                    currentItem.selected = true;
                    selectedNovels.add(currentItem);
                    notifyItemChanged(holder.getAdapterPosition());

                    libraryFragment.openMenu(selectedNovels);

                    return true;
                }


                for(NovelDetails n : selectedNovels){
                    if(n.position > highestValue && n.position < currentItem.position){
                        highestValue = n.position;
                    }

                    if(n.position < lowestValue && n.position < currentItem.position || lowestValue == -1){
                        lowestValue = n.position;
                    }
                }


                if(currentItem.position < lowestValue){
                    for(int i = 1; i <= lowestValue - currentItem.position; i++){
                        mNovelList.get(lowestValue - i).selected = true;
                        if(selectedNovels.contains(mNovelList.get(lowestValue - i))){
                            continue;
                        }
                        selectedNovels.add(mNovelList.get(lowestValue - i));
                    }

                    notifyItemRangeChanged(holder.getAdapterPosition(), lowestValue);
                    libraryFragment.update();

                    return true;
                }

                if(currentItem.position > highestValue){
                    for(int i = 1; i <= currentItem.position - highestValue; i++){
                        mNovelList.get(highestValue + i).selected = true;
                        if(selectedNovels.contains(mNovelList.get(highestValue + i))){
                            continue;
                        }
                        selectedNovels.add(mNovelList.get(highestValue + i));
                    }

                    notifyItemRangeChanged(highestValue + 1, currentItem.position);
                    libraryFragment.update();
                }

                System.out.println(selectedNovels.size());
                return true;
            }
        });

        holder.mImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                NovelDetails currentItem = mNovelList.get(holder.getAdapterPosition());

                if(!selectedNovels.isEmpty() && !currentItem.selected){
                    currentItem.selected = true;
                    if(selectedNovels.contains(currentItem)){
                        return;
                    }
                    selectedNovels.add(currentItem);
                    notifyItemChanged(currentItem.position);

                    libraryFragment.update();
                    return;
                }else if(!selectedNovels.isEmpty()){
                    currentItem.selected = false;
                    selectedNovels.remove(currentItem);
                    notifyItemChanged(currentItem.position);
                    libraryFragment.update();
                    return;
                }


                Intent intent = new Intent(ctx, NovelDetailsActivity.class);

                intent.putExtra("NovelDetails_name", currentItem.getNovelName());
                intent.putExtra("isFavorite", "yes");
                intent.putExtra("NovelDetails_source", currentItem.getSource());

                ctx.startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mNovelList.size();
    }
}
