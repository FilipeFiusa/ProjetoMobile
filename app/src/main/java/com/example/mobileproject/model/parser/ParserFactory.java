package com.example.mobileproject.model.parser;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobileproject.model.parser.english.FoxaholicParser;
import com.example.mobileproject.model.parser.english.GenesisTlParser;
import com.example.mobileproject.model.parser.english.LightNovelPubParser;
import com.example.mobileproject.model.parser.english.LightNovelReaderParser;
import com.example.mobileproject.model.parser.english.NovelFullDotNetParser;
import com.example.mobileproject.model.parser.english.NovelFullParser;
import com.example.mobileproject.model.parser.english.ReadLightNovelParser;
import com.example.mobileproject.model.parser.english.ReaperScansParser;
import com.example.mobileproject.model.parser.english.RoyalRoadParser;
import com.example.mobileproject.model.parser.english.WoopreadParser;
import com.example.mobileproject.model.parser.english.WuxiaBlogParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public static ParserInterface getParserInstanceWithAlternativeUrl(String alternativeUrl, Context ctx){
        List<Parser> allParsers = getAllParsers(ctx);

        for(Parser p : allParsers){
            if(p.getAlternativeUrls() != null && Arrays.stream(p.getAlternativeUrls()).allMatch(alternativeUrl::equals)){
                p.setUrlBase(alternativeUrl);
                System.out.println("found the alternative");
                return p;
            }
        }

        return null;


    }

    public static List<Parser> getAllParsers(Context ctx){
        return Arrays.asList(
                new NovelFullParser(ctx),
                new NovelFullDotNetParser(ctx),
                new LightNovelPubParser(ctx),
                new WuxiaBlogParser(ctx),
                new RoyalRoadParser(ctx),
                new FoxaholicParser(ctx),
                new GenesisTlParser(ctx),
                new LightNovelReaderParser(ctx),
                //new ReaperScansParser(ctx),
                new WoopreadParser(ctx),
                new ReadLightNovelParser(ctx)
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
            URL currentParserUrl = new URL(parser.getUrlBase());
            String currentUrlString = currentParserUrl.getProtocol() + "://" + currentParserUrl.getHost();

            System.out.println(currentUrlString);
            System.out.println(novelLinkString);
            System.out.println(currentUrlString.equals(novelLinkString));

            if (currentUrlString.equals(novelLinkString)){
                return parser;
            }

/*            if (parser.getAlternativeUrls() != null && parser.getAlternativeUrls().length > 0){
                for (String url : parser.getAlternativeUrls()){
                    currentParserUrl = new URL(url);
                    currentUrlString = currentParserUrl.getProtocol() + "://" + currentParserUrl.getHost();

                    if (currentUrlString.equals(novelLinkString)){
                        parser.setUrlBase(currentUrlString);
                        System.out.println(currentUrlString);
                        return parser;
                    }
                }
            }*/

        }

        return null;
    }
}
