package com.example.mobileproject.ui.navigate;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobileproject.R;
import com.example.mobileproject.VisitSourceActivity;
import com.example.mobileproject.model.parser.Parser;

import static android.app.PendingIntent.getActivity;


public class FontsGridAdaptor extends BaseAdapter {
    private Context ctx;
    private Parser[] lista;

    public FontsGridAdaptor(Context ctx, Parser[] lista){
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

        Parser currentItem = lista[position];

        TextView textView = (TextView) row.findViewById(R.id.source_name);
        textView.setText(currentItem.getSourceName());

        ImageView imageView = (ImageView) row.findViewById(R.id.favicon);
        imageView.setImageDrawable(currentItem.getIcon());

        Button simpleButton1 = (Button) row.findViewById(R.id.font_button_id);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, VisitSourceActivity.class);
                intent.putExtra("SourceName", currentItem.getSourceName());
                ctx.startActivity(intent);
            }
        });

        return row;
    }
}
