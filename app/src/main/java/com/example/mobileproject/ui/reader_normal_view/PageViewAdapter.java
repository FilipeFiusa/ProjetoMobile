package com.example.mobileproject.ui.reader_normal_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.UserReaderPreferences;
import com.example.mobileproject.model.ViewPageItem;
import com.example.mobileproject.util.FontFactory;
import com.example.mobileproject.util.TagReplacerHelper;

import java.util.ArrayList;

public class PageViewAdapter extends RecyclerView.Adapter<PageViewAdapter.ViewPagerCustomItem> {
    private ArrayList<ViewPageItem> viewPagerItemArrayList;
    final private ReaderActivity ctx;
    final private PageViewController controllerReference;
    final private UserReaderPreferences userReaderPreferences;

    public PageViewAdapter(ArrayList<ViewPageItem> viewPagerItemArrayList, ReaderActivity ctx,
                           PageViewController controller, UserReaderPreferences userReaderPreferences) {
        this.viewPagerItemArrayList = viewPagerItemArrayList;
        this.ctx = ctx;
        this.controllerReference = controller;
        this.userReaderPreferences = userReaderPreferences;
    }

    @NonNull
    @Override
    public ViewPagerCustomItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull ViewPagerCustomItem holder, int position) {
        final int itemType = getItemViewType(position);
        ViewPageItem currentItem = viewPagerItemArrayList.get(position);

        holder.previousButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerReference.onPreviousViewButtonClicked();
            }
        });

        holder.menuButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerReference.onMenuViewButtonClicked();
            }
        });

        holder.nextButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerReference.onNextViewButtonClicked();
            }
        });

        // First check here the View Type
        // than set data based on View Type to your recyclerview item
        if (itemType == 1) {
            TextAndTitleHolder textAndTitleHolder = (TextAndTitleHolder) holder;

            textAndTitleHolder.titleContainer.setText(currentItem.getTitle());
            textAndTitleHolder.textContainer.setText(TagReplacerHelper.replaceAll(ctx, currentItem.getChapterContent()));
            textAndTitleHolder.textContainer.setMovementMethod(LinkMovementMethod.getInstance());


            userReaderPreferences.applyPreferences(null, textAndTitleHolder.titleContainer, textAndTitleHolder.textContainer);

            if (currentItem.isLastPage()){
                textAndTitleHolder.titleContainer.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }

        } else if (itemType == 2) {
            TextOnlyHolder textOnlyHolder = (TextOnlyHolder) holder;

            textOnlyHolder.textContainer.setText(TagReplacerHelper.replaceAll(ctx, currentItem.getChapterContent()));
            textOnlyHolder.textContainer.setMovementMethod(LinkMovementMethod.getInstance());

            userReaderPreferences.applyPreferences(null, null, textOnlyHolder.textContainer);

            if (currentItem.isLastPage()){
                textOnlyHolder.textContainer.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        } else if (itemType == 3){
            NoAvailableChapter textOnlyHolder = (NoAvailableChapter) holder;

            if(currentItem.getChapter().getInvalidType() == 1){
                textOnlyHolder.textContainer.setText("Não há mais capitulo");
            }else if(currentItem.getChapter().getInvalidType() == 2){
                textOnlyHolder.textContainer.setText("Sem internet");
            }

            userReaderPreferences.applyPreferences(null, null, textOnlyHolder.textContainer);
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

    public abstract class ViewPagerCustomItem extends RecyclerView.ViewHolder{
        View previousButtonView;
        View menuButtonView;
        View nextButtonView;

        public ViewPagerCustomItem(@NonNull View itemView) {
            super(itemView);

            previousButtonView = itemView.findViewById(R.id.previous_page_view_button);
            menuButtonView = itemView.findViewById(R.id.menu_page_view_button);
            nextButtonView = itemView.findViewById(R.id.next_page_view_button);
        }
    }

    public class TextOnlyHolder extends ViewPagerCustomItem{
        TextView textContainer;

        public TextOnlyHolder(@NonNull View itemView) {
            super(itemView);
            textContainer = itemView.findViewById(R.id.chapter_content);
        }
    }

    public class TextAndTitleHolder extends ViewPagerCustomItem{
        TextView textContainer;
        TextView titleContainer;

        public TextAndTitleHolder(@NonNull View itemView) {
            super(itemView);

            textContainer = itemView.findViewById(R.id.chapter_content);
            titleContainer = itemView.findViewById(R.id.chapter_name);
        }
    }

    public class NoAvailableChapter extends ViewPagerCustomItem{
        TextView textContainer;

        public NoAvailableChapter(@NonNull View itemView) {
            super(itemView);

            textContainer = itemView.findViewById(R.id.chapter_content);

            previousButtonView = itemView.findViewById(R.id.previous_page_view_button);
            menuButtonView = itemView.findViewById(R.id.menu_page_view_button);
            nextButtonView = itemView.findViewById(R.id.next_page_view_button);
        }
    }
}