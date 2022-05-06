package com.example.mobileproject.util;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import com.example.mobileproject.ReaderActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagReplacerHelper {

    public static CharSequence replaceAll(ReaderActivity ctx, String text){
        CharSequence modifiedText = setTextToItalic(text);
        modifiedText = setTextToBold(modifiedText);
        modifiedText = setImageLink(modifiedText, ctx);


        return modifiedText;
    }

    public static CharSequence setTextToItalic(CharSequence text){
        Pattern pattern = Pattern.compile("->3(.*?)3<-");

        SpannableStringBuilder ssb = new SpannableStringBuilder( text );

        Matcher matcher = pattern.matcher( text );
        int matchesSoFar = 0;

        try {

            while (matcher.find()) {
                int start = matcher.start() - (matchesSoFar * 2);
                int end = matcher.end() - (matchesSoFar * 2);

                ssb.setSpan(
                        new StyleSpan(Typeface.ITALIC),
                        start + 3,
                        end - 3,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                ssb.delete(start, start + 3);
                ssb.delete(end - 6, end - 3);

                matchesSoFar = matchesSoFar + 3;
            }
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return ssb;
    }

    public static CharSequence setTextToBold(CharSequence text){
        Pattern pattern = Pattern.compile("->2(.*?)2<-");

        SpannableStringBuilder ssb = new SpannableStringBuilder( text );

        Matcher matcher = pattern.matcher( text );
        int matchesSoFar = 0;

        try {
            while (matcher.find()) {
                int start = matcher.start() - (matchesSoFar * 2);
                int end = matcher.end() - (matchesSoFar * 2);

                ssb.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        start + 3,
                        end - 3,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                ssb.delete(start, start + 3);
                ssb.delete(end - 6, end - 3);

                matchesSoFar = matchesSoFar + 3;
            }
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return ssb;
    }

    public static CharSequence setImageLink(CharSequence text, ReaderActivity ctx){
        Pattern pattern = Pattern.compile("<img(.*?)>");
        StringBuffer sb = new StringBuffer();
        SpannableStringBuilder ssb = new SpannableStringBuilder( text );

        Matcher matcher = pattern.matcher( ssb );
        int matchesSoFar = 0;

        try {
            while (matcher.find()) {
                sb.setLength(0);
                int start = matcher.start() - (matchesSoFar);
                int end = matcher.end() - (matchesSoFar);

                Document doc = Jsoup.parse(matcher.group());
                Element img = doc.select("img").get(0);

                String src = img.attr("src");
                String alt = "Image: " + img.attr("alt") + "\n\n";

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        ctx.loadImage(src);
                    }
                };

                int altSize = alt.length();

                ssb.replace(start, start + altSize, alt);

                ssb.setSpan(
                        clickableSpan,
                        start,
                        start + altSize - 2,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE );

                ssb.delete(start + altSize, end);

                matchesSoFar = matchesSoFar + (end - (start + altSize));
            }
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return ssb;
    }
}
