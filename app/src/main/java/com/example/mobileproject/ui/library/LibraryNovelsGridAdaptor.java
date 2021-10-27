package com.example.mobileproject.ui.library;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.NovelDetailsMinimum;
import com.example.mobileproject.model.NovelReaderController;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class LibraryNovelsGridAdaptor extends BaseAdapter {
        private Context ctx;
        private ArrayList<NovelDetails> lista;

        public LibraryNovelsGridAdaptor(Context ctx, ArrayList<NovelDetails> lista){
            this.ctx = ctx;
            this.lista = lista;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return lista.size();
        }

        @Override
        public NovelDetails getItem(int position) {
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
            chapterCount.setText(String.valueOf(getItem(position).getChapterToReadQuantity()));

            novelImage.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    Intent intent = new Intent(ctx, NovelDetailsActivity.class);

                    intent.putExtra("NovelDetails_name", getItem(position).getNovelName());
                    intent.putExtra("isFavorite", "yes");
                    intent.putExtra("NovelDetails_source", getItem(position).getSource());

                    ctx.startActivity(intent);
                }
            });

            return row;
        }
    }
