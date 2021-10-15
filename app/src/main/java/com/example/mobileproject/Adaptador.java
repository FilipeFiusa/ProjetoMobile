package com.example.mobileproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

public class Adaptador extends BaseAdapter {
    private Context ctx;
    private int[] lista;

    public Adaptador(Context ctx, int[] lista){
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



        RelativeLayout row = (RelativeLayout) (convertView == null
                                ? LayoutInflater.from(ctx).inflate(R.layout.novel_grid_button, parent, false)
                                : convertView);

        return row;
    }
}
