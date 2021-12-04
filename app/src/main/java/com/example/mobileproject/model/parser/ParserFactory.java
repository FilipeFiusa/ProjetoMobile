package com.example.mobileproject.model.parser;

import android.content.Context;

import com.example.mobileproject.model.parser.english.LightNovelPubParser;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.model.parser.english.WuxiaBlogParser;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ParserFactory {
    public static ParserInterface getParserInstance(String sourceName, Context ctx){
        List<Parser> allParsers = getAllParsers(ctx);

        for(Parser p : allParsers){
            System.out.println(p.getSourceName());
            if(p.getSourceName().equals(sourceName)){
                return p.getParserInstance();
            }
        }

        return null;
    }

    public static List<Parser> getAllParsers(Context ctx){
        return Arrays.asList(
                new NovelFullParser(ctx),
                new LightNovelPubParser(ctx),
                new WuxiaBlogParser(ctx)
        );
    }
}
