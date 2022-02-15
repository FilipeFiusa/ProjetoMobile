package com.example.mobileproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.model.ImageWebViewResource;

import java.util.ArrayList;

public class WebViewImagesAdapter extends RecyclerView.Adapter<WebViewImagesAdapter.ImageViewHolder> {
    private ArrayList<ImageWebViewResource> mImageList;
    private CreateSourceWebViewActivity ctx;

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;

        public View container;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.current_image);
            mTextView1 = itemView.findViewById(R.id.current_alt);
            mTextView2 = itemView.findViewById(R.id.current_src);

            container = itemView;
        }
    }

    public WebViewImagesAdapter(ArrayList<ImageWebViewResource> mImageList, CreateSourceWebViewActivity ctx) {
        this.mImageList = mImageList;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.create_source_recycle_view_image_item, parent, false);;
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageWebViewResource currentImage = mImageList.get(position);

        currentImage.position = holder.getAdapterPosition();

        if(currentImage.getImageResource() != null){
            holder.mImageView.setImageBitmap(currentImage.getImageResource());
        }

        holder.mTextView1.setText(String.format("Alt: %s", currentImage.getAlt()));
        holder.mTextView2.setText(String.format("Src: %s", currentImage.getImageResourceLink()));

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentImage.getImageResource() != null){
                    ctx.updateCurrentImage(currentImage.getImageResource());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }



}
