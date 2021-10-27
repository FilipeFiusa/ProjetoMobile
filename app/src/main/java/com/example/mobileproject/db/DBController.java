package com.example.mobileproject.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.model.ChapterContent;
import com.example.mobileproject.model.ChapterInDownload;
import com.example.mobileproject.model.ChapterIndex;
import com.example.mobileproject.model.DownloaderClass;
import com.example.mobileproject.model.NovelDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DBController {
    private Context ctx;
    private SQLiteDatabase db = null;
    private CreateDB database;

    public DBController(Context ctx){
        database = CreateDB.getInstance(ctx);
        this.ctx = ctx;
    }

    public String insertNovel(String novelName, String novelAuthor,String novelDescription, String source, Bitmap novelImage, String novelLink){
        ContentValues values;
        long result;

        db = database.getWritableDatabase();
        values = new ContentValues();

        values.put("novel_name", novelName);
        values.put("novel_author", novelAuthor);
        values.put("novel_description", novelDescription);
        values.put("novel_source", source);
        values.put("novel_link", novelLink);

        try (FileOutputStream fos = ctx.openFileOutput(novelName + "_" + source + "_" + "image.png", Context.MODE_PRIVATE)) {
            novelImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }catch (IOException e) {
            e.printStackTrace();
        }

        values.put("novel_image", novelName + "_" + source + "_" + "image.png");

        result = db.insert("Novels", null, values);
        db.close();

        String[] files = ctx.fileList();

        for (String f : files){
            Log.i("-------", f);
        }

        if(result ==  -1){
            return "Error on insert";
        }else{
            return "Sucess";
        }
    }

    public String insertChapters(String novelName, String novelSource, ChapterIndex index) {
        ContentValues values;
        long result;

        if(db == null){
            db = database.getWritableDatabase();
        }

        values = new ContentValues();

        values.put("novel_name", novelName);
        values.put("novel_source", novelSource);
        values.put("source_id", index.getSourceId());
        values.put("chapter_name", index.getChapterName());
        values.put("chapter_link", index.getChapterLink());
        values.put("chapter_content", "");
        values.put("readed", "no");
        values.put("downloaded", "no");

        result = db.insert("Chapters", null, values);
        db.close();

        if(result ==  -1){
            return "Error on insert";
        }else{
            return "Sucess";
        }

    }

    public void updateChapters(String novelName, String novelSource, ArrayList<ChapterIndex> c){
        Cursor result;
        long result2;

        for (int i = 0; i < c.size(); i++) {
            db = database.getReadableDatabase();

            ContentValues values = new ContentValues();
            Cursor cursor;

            String sql = "SELECT * FROM Chapters WHERE id=?";
            if(c.get(i).getId() == -1){
                cursor = null;
            }else{
                cursor = db.rawQuery(sql, new String[]{String.valueOf(c.get(i).getId())});
            }


            if (cursor == null || !cursor.moveToFirst()) {
                //Insert new
                System.out.println("Inserting");

                insertChapters(novelName, novelSource, c.get(i));
            } else {
                //Update
                System.out.println("Updating");

                values.put("source_id", c.get(i).getSourceId());
                db.update("Chapters", values, "id=?", new String[]{String.valueOf(c.get(i).getId())});
            }
            db.close();
        }
    }

    public ArrayList<NovelDetails> selectAllNovels(){
        Cursor result;
        ArrayList<NovelDetails> novelDetailsArr = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Novels";
        result = db.rawQuery(query, null);

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                NovelDetails novelDetails = new NovelDetails();

                novelDetails.setNovelAuthor(result.getString(result.getColumnIndexOrThrow("novel_author")));
                novelDetails.setNovelName(result.getString(result.getColumnIndexOrThrow("novel_name")));
                novelDetails.setNovelDescription(result.getString(result.getColumnIndexOrThrow("novel_description")));
                novelDetails.setSource(result.getString(result.getColumnIndexOrThrow("novel_source")));
                novelDetails.setNovelLink(result.getString(result.getColumnIndexOrThrow("novel_link")));


                String filePath = result.getString(result.getColumnIndexOrThrow("novel_image"));
                File mSaveBit = new File(ctx.getFilesDir(), filePath);;
                String imagePath = mSaveBit.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                novelDetails.setNovelImage(bitmap);

                query = "SELECT Count(*) FROM Chapters WHERE novel_name=? AND novel_source=? AND readed=?";
                Cursor result2 = db.rawQuery(query, new String[]{novelDetails.getNovelName(), novelDetails.getSource(), "no"});
                if(result2.getCount() > 0) {
                    result2.moveToFirst();

                    novelDetails.setChapterToReadQuantity(result2.getInt(result2.getColumnIndexOrThrow("Count(*)")));
                }

                novelDetailsArr.add(novelDetails);
            }while (result.moveToNext());
        }

        db.close();

        return novelDetailsArr;
    }

    public NovelDetails getNovel(String novelName, String novelSource){
        Cursor result;
        NovelDetails novel = new NovelDetails();

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Novels WHERE novel_name=? AND novel_source=?";
        result = db.rawQuery(query, new String[]{novelName, novelSource});
        if(result.getCount() > 0){
            result.moveToFirst();

            novel.setNovelAuthor(result.getString(result.getColumnIndexOrThrow("novel_author")));
            novel.setNovelName(result.getString(result.getColumnIndexOrThrow("novel_name")));
            novel.setNovelDescription(result.getString(result.getColumnIndexOrThrow("novel_description")));
            novel.setSource(result.getString(result.getColumnIndexOrThrow("novel_source")));
            novel.setNovelLink(result.getString(result.getColumnIndexOrThrow("novel_link")));

            String filePath = result.getString(result.getColumnIndexOrThrow("novel_image"));
            File mSaveBit = new File(ctx.getFilesDir(), filePath);;
            String imagePath = mSaveBit.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            novel.setNovelImage(bitmap);
        }else{
            db.close();
            return null;
        }

        db.close();

        return novel;
    }

    public ArrayList<ChapterIndex> getChaptersFromANovel(String novelName, String novelSource){
        Cursor result;
        ArrayList<ChapterIndex> chapterIndexes = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "SELECT id, chapter_link, chapter_name, downloaded, readed FROM Chapters WHERE Chapters.novel_name=? AND Chapters.novel_source=? ORDER BY source_id ASC";
        result = db.rawQuery(query, new String[]{novelName, novelSource});

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                ChapterIndex c = new ChapterIndex();

                c.setId(result.getInt(result.getColumnIndexOrThrow("id")));
                c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
                c.setChapterLink(result.getString(result.getColumnIndexOrThrow("chapter_link")));

                c.setDownloaded(result.getString(result.getColumnIndexOrThrow("downloaded")));
                c.setReaded(result.getString(result.getColumnIndexOrThrow("readed")));

                chapterIndexes.add(c);
            }while (result.moveToNext());
        }

        db.close();

        return chapterIndexes;
    }

    public ArrayList<ChapterInDownload>  getDownloadingChapters(){
        Cursor result;
        ArrayList<ChapterInDownload> chapterIndexes = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "" +
                "SELECT " +
                "Chapters.id, Chapters.chapter_name,Chapters.novel_name, Novels.novel_image " +
                "FROM Chapters " +
                "INNER JOIN Novels on Novels.novel_name = Chapters.novel_name AND Novels.novel_source = Chapters.novel_source " +
                "WHERE Chapters.downloaded=?";

        result = db.rawQuery(query, new String[]{"downloading"});

        if(result.getCount() > 0){
            result.moveToFirst();

            String filePath = result.getString(result.getColumnIndexOrThrow("novel_image"));
            File mSaveBit = new File(ctx.getFilesDir(), filePath);;
            String imagePath = mSaveBit.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            do {
                ChapterIndex c = new ChapterIndex();

                c.setId(result.getInt(result.getColumnIndexOrThrow("id")));
                c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
                String novel_name = result.getString(result.getColumnIndexOrThrow("novel_name"));

                chapterIndexes.add(new ChapterInDownload(novel_name, bitmap, c));
            }while (result.moveToNext());

        }

        db.close();

        return chapterIndexes;
    }

    public boolean putChapterOnDownload(int id){
        long result;
        ContentValues values = new ContentValues();;

        db = database.getReadableDatabase();

        values.put("downloaded", "downloading");

        result = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        if(result ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean putChapterOnDownload(String novelName, String novelSource, int id){
        long result;
        ContentValues values = new ContentValues();;

        db = database.getReadableDatabase();

        values.put("downloaded", "downloading");
        result = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        if(result ==  -1){
            return false;
        }

        values = new ContentValues();

        values.put("novel_name", novelName);
        values.put("novel_source", novelSource);
        values.put("chapter_id", id);

        result = db.insert("DownloadQueue", null, values);

        db.close();

        if(result ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean setChapterAsReaded(int id){
        long result;
        ContentValues values = new ContentValues();;

        db = database.getReadableDatabase();

        values.put("readed", "yes");
        result = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        db.close();

        if(result ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public ArrayList<DownloaderClass> getDownloadingNovels(){
        Cursor result;
        ArrayList<DownloaderClass> downloaderClasses = new ArrayList<>();
        db = database.getReadableDatabase();


        ArrayList<ChapterIndex> chapterIndexes = new ArrayList<>();

        String query = "" +
                "SELECT " +
                "Chapters.id, Chapters.chapter_name,Chapters.chapter_link, Novels.novel_name, Novels.novel_source " +
                "FROM Chapters " +
                "INNER JOIN Novels on Novels.novel_name = Chapters.novel_name AND Novels.novel_source = Chapters.novel_source " +
                "INNER JOIN DownloadQueue on DownloadQueue.chapter_id = Chapters.id " +
                "WHERE Chapters.downloaded=?" +
                "ORDER BY DownloadQueue.id ASC";

        result = db.rawQuery(query, new String[]{"downloading"});

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                ChapterIndex c = new ChapterIndex();

                c.setId(result.getInt(result.getColumnIndexOrThrow("id")));
                c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
                c.setChapterLink(result.getString(result.getColumnIndexOrThrow("chapter_link")));
                c.setDownloaded("downloading");

                String novelName = result.getString(result.getColumnIndexOrThrow("novel_name"));
                String novelSource = result.getString(result.getColumnIndexOrThrow("novel_source"));

                downloaderClasses.add(new DownloaderClass(novelName, novelSource, c));

            }while (result.moveToNext());
        }


        return downloaderClasses;
    }

    public ChapterContent getChapter(int id){
        Cursor result;

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Chapters WHERE Chapters.id=? ";
        result = db.rawQuery(query, new String[]{String.valueOf(id)});

        if(result.getCount() > 0){
            result.moveToFirst();

            ChapterContent c = new ChapterContent();

            c.setChapterContent(result.getString(result.getColumnIndexOrThrow("chapter_content")));
            c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
            c.setChapterLink(result.getString(result.getColumnIndexOrThrow("chapter_link")));

            db.close();
            return c;
        }

        db.close();
        return null;
    }

    public boolean setChapterContent(int id, String chapterContent){
        long result;
        db = database.getReadableDatabase();

        ContentValues values = new ContentValues();;
        values.put("chapter_content", chapterContent);
        values.put("downloaded", "yes");

        result = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        if(result ==  -1){
            return false;
        }

        result = db.delete("DownloadQueue", "chapter_id=?", new String[]{String.valueOf(id)});

        db.close();

        if(result ==  -1){
            return false;
        }else{
            return true;
        }
    }
}
