package com.example.mobileproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class SearchNovelGridAdapter extends BaseAdapter {
    private Context ctx;
    private int[] lista;

    public SearchNovelGridAdapter(Context ctx, int[] lista){
        this.ctx = ctx;
        this.lista = lista;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return lista.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return lista[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub



        LinearLayout row = (LinearLayout) (convertView == null
                ? LayoutInflater.from(ctx).inflate(R.layout.search_in_source_grid, parent, false)
                : convertView);

        return row;
    }
}
