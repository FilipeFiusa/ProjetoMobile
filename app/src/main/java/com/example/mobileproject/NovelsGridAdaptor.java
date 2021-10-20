package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mobileproject.model.NovelDetailsMinimum;

import java.util.ArrayList;

public class NovelsGridAdaptor extends BaseAdapter {
        private Context ctx;
        private ArrayList<NovelDetailsMinimum> lista;

        public NovelsGridAdaptor(Context ctx, ArrayList<NovelDetailsMinimum> lista){
            this.ctx = ctx;
            this.lista = lista;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return lista.size();
        }

        @Override
        public NovelDetailsMinimum getItem(int position) {
            // TODO Auto-generated method stub
            return lista.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            RelativeLayout row = (RelativeLayout) (convertView == null
                    ? LayoutInflater.from(ctx).inflate(R.layout.novel_grid_button, parent, false)
                    : convertView);

            ImageView novelImage = (ImageView) row.findViewById(R.id.grid_novel_image);
            TextView novelTitle = (TextView) row.findViewById(R.id.grid_novel_name);
            TextView chapterCount = (TextView) row.findViewById(R.id.grid_chapter_count);

            novelTitle.setText(getItem(position).getNovelName());
            novelImage.setImageBitmap(getItem(position).getNovelImage());
            chapterCount.setVisibility(View.INVISIBLE);

            novelImage.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    Intent intent = new Intent(ctx, NovelDetailsActivity.class);
                    intent.putExtra("novelLink", getItem(position).getNovelLink());
                    intent.putExtra("novelName", getItem(position).getNovelName());
                    intent.putExtra("novelSource", "NovelFull");
                    ctx.startActivity(intent);
                }
            });

            return row;
        }
    }
