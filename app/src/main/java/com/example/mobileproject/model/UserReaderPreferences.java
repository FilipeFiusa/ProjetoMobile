package com.example.mobileproject.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.example.mobileproject.R;
import com.example.mobileproject.util.FontFactory;

import java.util.ArrayList;

public class UserReaderPreferences {
    private float fontSize;
    private String fontName;
    private String fontColor;
    private String backgroundColor;

    private Context ctx;

    public UserReaderPreferences(Context ctx) {
        this.ctx = ctx;

        SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);

        backgroundColor = preferences.getString("background_color", "#1F1B1B");
        fontSize = preferences.getFloat("font_size", 18);
        fontName = preferences.getString("font_name", "Roboto");
        fontColor = preferences.getString("font_color", "#FFFFFF");
    }

    public void setUserReaderPreferences(float fontSize, String fontName, String fontColor, String backgroundColor){
        SharedPreferences preferences = ctx.getSharedPreferences("readerPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(backgroundColor != null){
            this.backgroundColor = backgroundColor;
            editor.putString("background_color", backgroundColor);
        }

        if(fontSize != 0){
            this.fontSize = fontSize;
            editor.putFloat("font_size", fontSize);
        }

        if(fontName != null){
            this.fontName = fontName;
            editor.putString("font_name", fontName);
        }

        if(fontColor != null) {
            this.fontColor = fontColor;
            editor.putString("font_color", fontColor);
        }

        editor.apply();
    }

    public void applyPreferences(View background, TextView titleTextView, TextView chapterTextView){
        if(background != null){
            background.setBackgroundColor(Color.parseColor(backgroundColor));
        }

        if(titleTextView != null){
            titleTextView.setTextSize(fontSize + 15);
            titleTextView.setTypeface(new FontFactory().getFont(fontName, ctx));
            titleTextView.setTextColor(Color.parseColor(fontColor));
        }

        if(chapterTextView != null){
            chapterTextView.setTextSize(fontSize);
            chapterTextView.setTypeface(new FontFactory().getFont(fontName, ctx));
            chapterTextView.setTextColor(Color.parseColor(fontColor));
        }
    }
}
