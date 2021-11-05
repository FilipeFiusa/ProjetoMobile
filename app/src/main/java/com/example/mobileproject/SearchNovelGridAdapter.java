package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class SearchNovelGridAdapter extends RecyclerView.Adapter<SearchNovelGridAdapter.NovelGridViewHolder> {

    private final ArrayList<ParserTempClass> mTempParser = new ArrayList<>();
    private List<Parser> mParserList;
    private String SearchText;
    AppCompatActivity ctx;

    public static class NovelGridViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView1;
        public LinearLayout mContainer;

        public NovelGridViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextView1 = itemView.findViewById(R.id.source_name);
            mContainer = itemView.findViewById(R.id.novels_container);
        }
    }

    public SearchNovelGridAdapter(AppCompatActivity ctx, List<Parser> mList, String SearchText) {
        this.ctx = ctx;
        this.mParserList = mList;
        this.SearchText = SearchText;

        new Thread(new SearchInAllSourcesWorker()).start();
    }

    @NonNull
    @Override
    public NovelGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.search_in_source_grid, parent, false);;
        return new NovelGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelGridViewHolder holder, int position) {
        Parser currentItem = mParserList.get(position);
        holder.mTextView1.setText(currentItem.getSourceName());
        mTempParser.add(new ParserTempClass(currentItem, holder.mContainer));
    }


    @Override
    public int getItemCount() {
        return mParserList.size();
    }

    private class SearchInAllSourcesWorker implements Runnable {
        @Override
        public void run() {
            SystemClock.sleep(2000);

            ArrayList<ParserTempClass> tempList = new ArrayList<>(mTempParser);

            while (!tempList.isEmpty()){
                ParserTempClass current = tempList.get(0);

                ArrayList<NovelDetailsMinimum> novelDetailsArr = current.parser.searchNovels(SearchText);
                UpdateView(current.novelsContainer, novelDetailsArr, current.parser.getSourceName());

                tempList.remove(current);
            }
        }

        public void UpdateView(LinearLayout view, ArrayList<NovelDetailsMinimum> novelDetailsArr, String novelSource){
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.removeAllViews();

                    for (int i = 0; i < novelDetailsArr.size(); i++) {
                        NovelDetailsMinimum n = novelDetailsArr.get(i);

                        View item = InflateView(n);

                        view.addView(item);
                    }
                }

                public View InflateView(NovelDetailsMinimum n){
                    View newItem = LayoutInflater.from(ctx).inflate(R.layout.search_novel_grid_button,
                            view, false);

                    TextView textView = (TextView) newItem.findViewById(R.id.novel_name);
                    ImageView mImage = (ImageView) newItem.findViewById(R.id.novel_image);

                    textView.setText(n.getNovelName());
                    mImage.setImageBitmap(n.getNovelImage());

                    newItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, NovelDetailsActivity.class);
                            intent.putExtra("novelLink", n.getNovelLink());
                            intent.putExtra("novelName", n.getNovelName());
                            intent.putExtra("novelSource", novelSource);
                            ctx.startActivity(intent);
                        }
                    });

                    return newItem;
                }
            });
        }
    }

    private static class ParserTempClass{
        public Parser parser;
        public LinearLayout novelsContainer;

        public ParserTempClass(Parser parser, LinearLayout novelsContainer) {
            this.parser = parser;
            this.novelsContainer = novelsContainer;
        }
    }
}
