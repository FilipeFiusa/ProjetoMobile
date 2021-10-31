package com.example.mobileproject.ui.navigate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobileproject.R;
import com.example.mobileproject.VisitSourceActivity;

import static android.app.PendingIntent.getActivity;


public class FontsGridAdaptor extends BaseAdapter {
    private Context ctx;
    private int[] lista;

    public FontsGridAdaptor(Context ctx, int[] lista){
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
                ? LayoutInflater.from(ctx).inflate(R.layout.font_grid_view, parent, false)
                : convertView);

        TextView textView = (TextView) row.findViewById(R.id.source_name);
        textView.setText("Novelfull.com");
        Button simpleButton1 = (Button) row.findViewById(R.id.font_button_id);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.startActivity(new Intent(ctx, VisitSourceActivity.class));
            }
        });

        return row;
    }
}
