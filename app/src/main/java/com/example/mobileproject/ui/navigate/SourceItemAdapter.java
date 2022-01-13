package com.example.mobileproject.ui.navigate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.VisitSourceActivity;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.util.FontFactory;

import java.util.ArrayList;

public class SourceItemAdapter extends RecyclerView.Adapter<SourceItemAdapter.SourceItemViewHolder> {
    private Context ctx;
    private ArrayList<Parser> mParsers;
    private SourceContainerAdapter adapterReference;
    private boolean isFixed = false;


    public SourceItemAdapter(Context ctx, ArrayList<Parser> mParsers, SourceContainerAdapter adapterReference) {
        this.ctx = ctx;
        this.mParsers = mParsers;
        this.adapterReference = adapterReference;
    }

    public SourceItemAdapter(Context ctx, ArrayList<Parser> mParsers, SourceContainerAdapter adapterReference, boolean isFixed) {
        this.ctx = ctx;
        this.mParsers = mParsers;
        this.adapterReference = adapterReference;
        this.isFixed = isFixed;
    }

    public void insertItem(Parser parser){
        int posToAdd = mParsers.size();

        for (int i = 0; i < mParsers.size(); i++){
            Parser currentItem = mParsers.get(i);

            if(parser.getSourceName().compareToIgnoreCase(currentItem.getSourceName()) <= 0) {
                posToAdd = i;
                break;
            }
        }

        mParsers.add(posToAdd, parser);
        notifyItemInserted(posToAdd);
    }

    public void removeItem(Parser parser){
        int posToRemove = 0;

        for (int i = 0; i < mParsers.size(); i++){
            Parser currentItem = mParsers.get(i);

            if(currentItem == parser) {
                posToRemove = i;
                break;
            }
        }

        mParsers.remove(posToRemove);
        notifyItemRemoved(posToRemove);

        parser.adapterReference.notifyItemChanged(parser.position);
    }

    @NonNull
    @Override
    public SourceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.navigate_source_item, parent, false);;
        return new SourceItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SourceItemViewHolder holder, int position) {
        Parser currentItem = mParsers.get(position);

        if (!isFixed){
            currentItem.adapterReference = this;
            currentItem.position = holder.getAdapterPosition();
        }

        holder.mTextView.setText(currentItem.getSourceName());
        holder.mImageView.setImageDrawable(currentItem.getIcon());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, VisitSourceActivity.class);
                intent.putExtra("SourceName", currentItem.getSourceName());
                ctx.startActivity(intent);
            }
        });

        if(currentItem.isPinned){
            holder.mImageButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_baseline_push_pin_24));
        }else {
            holder.mImageButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_outline_push_pin_24));
        }

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentItem.isPinned = !currentItem.isPinned;
                if(isFixed){
                    currentItem.adapterReference.notifyItemChanged(currentItem.position);
                }else {
                    notifyItemChanged(holder.getAdapterPosition());
                }

                SharedPreferences preferences = ctx.getSharedPreferences(ParserFactory.sharedPreferencesName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String parserNameInPreferences = currentItem.getSourceName() + "-" + currentItem.getLanguage().getAbbreviatedName();
                editor.putBoolean(parserNameInPreferences + "-isPinned", currentItem.isPinned);
                editor.apply();

/*                if(currentItem.isPinned){
                    adapterReference.sourceItemFixed(currentItem);
                }else {
                    adapterReference.sourceItemUnFixed(currentItem);
                }*/

                if(currentItem.isPinned){
                    adapterReference.sourceItemFixed(currentItem);
                }else {
                    adapterReference.sourceItemUnFixed(currentItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mParsers.size();
    }

    public static class SourceItemViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public TextView mTextView;
        public ImageView mImageView;
        public ImageButton mImageButton;

        public SourceItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            mTextView = (TextView) itemView.findViewById(R.id.source_name);
            mImageView = (ImageView) itemView.findViewById(R.id.favicon);
            mImageButton = (ImageButton) itemView.findViewById(R.id.pin_source);
        }
    }
}
