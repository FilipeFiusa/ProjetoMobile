package com.example.mobileproject.ui.library;

import android.graphics.Rect;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class NovelsLayoutDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public NovelsLayoutDecoration(int space, AppCompatActivity ctx) {
        this.space = Math.round(space * ctx.getResources().getDisplayMetrics().density);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
        outRect.top = space;
    }
}
