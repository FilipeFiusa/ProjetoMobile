package com.example.mobileproject.util;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.example.mobileproject.R;

public class FontFactory {

    private String[] fonts = new String[]{
            "Acme",
            "Alice",
            "Coming_soon",
            "Roboto"
    };

    public FontFactory() {
    }

    public String[] getAvailableFontsInString(){
        return fonts;
    }

    public Typeface getFont(String font_name, Context ctx){
        Typeface font = null;

        if(font_name.equals("Acme")){
            font = ResourcesCompat.getFont(ctx, R.font.acme);
        }else if(font_name.equals("Alice")){
            font = ResourcesCompat.getFont(ctx, R.font.alice);
        }else if(font_name.equals("Coming_soon")){
            font = ResourcesCompat.getFont(ctx, R.font.coming_soon);
        }else if(font_name.equals("Roboto")){
            font = ResourcesCompat.getFont(ctx, R.font.roboto);
        }

        return font;
    }
}
