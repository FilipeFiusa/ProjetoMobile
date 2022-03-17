package com.example.mobileproject.ui.reader_normal_view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.model.ViewPageItem;
import com.example.mobileproject.util.FontFactory;

import java.util.ArrayList;

public class PageViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ViewPageItem> viewPagerItemArrayList;
    final private AppCompatActivity ctx;
    final private PageViewController controllerReference;

    public PageViewAdapter(ArrayList<ViewPageItem> viewPagerItemArrayList, AppCompatActivity ctx, PageViewController controller) {
        this.viewPagerItemArrayList = viewPagerItemArrayList;
        this.ctx = ctx;
        this.controllerReference = controller;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (viewType == 1) {
            view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.reader_normal_view_page_view_item1, parent, false);;
            return new TextAndTitleHolder(view);
        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reader_normal_view_page_view_item2, parent, false);
            return new TextOnlyHolder(view);
        } else if (viewType == 3) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reader_normal_view_page_view_item3, parent, false);
            return new NoAvailableChapter(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final int itemType = getItemViewType(position);
        ViewPageItem currentItem = viewPagerItemArrayList.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerReference.onClicked();
            }
        });

        // First check here the View Type
        // than set data based on View Type to your recyclerview item
        if (itemType == 1) {
            TextAndTitleHolder textAndTitleHolder = (TextAndTitleHolder) holder;

            textAndTitleHolder.textContainer.setText(currentItem.getChapterContent());
            textAndTitleHolder.titleContainer.setText(currentItem.getTitle());

            SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

            textAndTitleHolder.textContainer.setTextSize(preferences.getFloat("font_size", 18));
            textAndTitleHolder.textContainer.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
            textAndTitleHolder.textContainer.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));

            textAndTitleHolder.titleContainer.setTextSize(preferences.getFloat("font_size", 20) + 15);
            textAndTitleHolder.titleContainer.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
            textAndTitleHolder.titleContainer.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));

            textAndTitleHolder.titleContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controllerReference.onClicked();
                }
            });

            textAndTitleHolder.textContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controllerReference.onClicked();
                }
            });

        } else if (itemType == 2) {
            TextOnlyHolder textOnlyHolder = (TextOnlyHolder) holder;

            textOnlyHolder.textContainer.setText(currentItem.getChapterContent());

            SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

            textOnlyHolder.textContainer.setTextSize(preferences.getFloat("font_size", 18));
            textOnlyHolder.textContainer.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
            textOnlyHolder.textContainer.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));

            textOnlyHolder.textContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controllerReference.onClicked();
                }
            });

        } else if (itemType == 3){
            NoAvailableChapter textOnlyHolder = (NoAvailableChapter) holder;

            if(currentItem.getChapter().getInvalidType() == 1){
                textOnlyHolder.textContainer.setText("Não há mais capitulo");
            }else if(currentItem.getChapter().getInvalidType() == 2){
                textOnlyHolder.textContainer.setText("Sem internet");
            }

            SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

            textOnlyHolder.textContainer.setTextSize(preferences.getFloat("font_size", 18));
            textOnlyHolder.textContainer.setTypeface(new FontFactory().GetFont(preferences.getString("font_name", "Roboto"), ctx));
            textOnlyHolder.textContainer.setTextColor(Color.parseColor(preferences.getString("font_color", "#FFFFFF")));

            textOnlyHolder.textContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controllerReference.onClicked();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        // based on you list you will return the ViewType
        return viewPagerItemArrayList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return viewPagerItemArrayList.size();
    }

    public class TextOnlyHolder extends RecyclerView.ViewHolder{
        TextView textContainer;

        public TextOnlyHolder(@NonNull View itemView) {
            super(itemView);
            textContainer = itemView.findViewById(R.id.chapter_content);
        }
    }

    public class TextAndTitleHolder extends RecyclerView.ViewHolder{
        TextView textContainer;
        TextView titleContainer;

        public TextAndTitleHolder(@NonNull View itemView) {
            super(itemView);

            textContainer = itemView.findViewById(R.id.chapter_content);
            titleContainer = itemView.findViewById(R.id.chapter_name);
        }
    }

    public class NoAvailableChapter extends RecyclerView.ViewHolder{
        TextView textContainer;

        public NoAvailableChapter(@NonNull View itemView) {
            super(itemView);

            textContainer = itemView.findViewById(R.id.chapter_content);
        }
    }
}