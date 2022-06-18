package com.example.mobileproject.model.parser;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobileproject.model.parser.english.FoxaholicParser;
import com.example.mobileproject.model.parser.english.LightNovelPubParser;
import com.example.mobileproject.model.parser.english.LightNovelReaderParser;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.model.parser.english.ReaperScansParser;
import com.example.mobileproject.model.parser.english.RoyalRoadParser;
import com.example.mobileproject.model.parser.english.WoopreadParser;
import com.example.mobileproject.model.parser.english.WuxiaBlogParser;

import org.reflections.Reflections;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ParserFactory {
    public static final String sharedPreferencesName = "parserPreferences";

    public static ParserInterface getParserInstance(String sourceName, Context ctx){
        List<Parser> allParsers = getAllParsers(ctx);

        for(Parser p : allParsers){
            if(p.getSourceName().equals(sourceName)){
                return p.getParserInstance();
            }
        }

        return null;
    }

    public static List<Parser> getAllParsers(Context ctx){
        return Arrays.asList(
                new NovelFullParser(ctx),
                //new LightNovelPubParser(ctx),
                new WuxiaBlogParser(ctx),
                new RoyalRoadParser(ctx),
                new FoxaholicParser(ctx),
                new LightNovelReaderParser(ctx),
                new ReaperScansParser(ctx),
                new WoopreadParser(ctx)
        );
    }

    public static List<Parser> getAllParsersWithPreferences(Context ctx){
        List<Parser> parsers = getAllParsers(ctx);
        SharedPreferences parserPreferences = ctx.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);

        Collections.sort(parsers, new Comparator<Parser>() {
            @Override
            public int compare(Parser p1, Parser p2) {
                return p1.getSourceName().compareToIgnoreCase(p2.getSourceName());
            }
        });

        for (Parser parser : parsers){
            String parserNameInPreferences = parser.SourceName + "-" + parser.getLanguage().getAbbreviatedName();

            parser.isPinned = parserPreferences.getBoolean(parserNameInPreferences + "-isPinned", false);
            parser.isActive = parserPreferences.getBoolean(parserNameInPreferences + "-isActive", true);
        }


        return parsers;
    }

    //Academy undecorver - 32

    public static Parser checkIfSourceExistsWithLink(Context ctx, URL novelLink) throws MalformedURLException {
        List<Parser> allParsers = getAllParsers(ctx);

        String novelLinkString = novelLink.getProtocol() + "://" + novelLink.getHost();

        for (Parser parser : allParsers){
            URL currentParserUrl = new URL(parser.getURL_BASE());
            String currentUrlString = currentParserUrl.getProtocol() + "://" + currentParserUrl.getHost();

            if (currentUrlString.equals(novelLinkString)){
                return parser;
            }
        }

        return null;
    }
}
