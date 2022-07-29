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
import com.example.mobileproject.model.ChapterStatus;
import com.example.mobileproject.model.DownloaderClass;
import com.example.mobileproject.model.NovelCleaner;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.model.epub.BookDetails;
import com.example.mobileproject.model.epub.EpubChapter;

import org.jsoup.safety.Cleaner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class DBController {
    private Context ctx;
    private SQLiteDatabase db = null;
    private CreateDB database;

    public DBController(Context ctx){
        database = CreateDB.getInstance(ctx);
        this.ctx = ctx;
    }

    public synchronized long insertNovel(NovelDetails n){
        //String novelName, String novelAuthor,String novelDescription, String source, Bitmap novelImage, String novelLink

        ContentValues values;
        long result;

        db = database.getWritableDatabase();
        values = new ContentValues();

        values.put("last_readed", new Date().getTime());//last_readed
        values.put("novel_type", 1);
        values.put("on_library", 0);//last_readed
        values.put("readerViewType", 1);//last_readed
        values.put("last_page_searched", n.getLastPageSearched());//last_readed
        values.put("novel_name", n.getNovelName());
        values.put("novel_author", n.getNovelAuthor());
        values.put("novel_description", n.getNovelDescription());
        values.put("novel_source", n.getSource());
        values.put("order_type", "DSC");
        values.put("novel_link", n.getNovelLink());
        values.put("status", n.getStatus());
        values.put("finished_loading", 0);

        try (FileOutputStream fos = ctx.openFileOutput(n.getNovelName() + "_" + n.getSource() + "_" + "image.png", Context.MODE_PRIVATE)) {
            n.getNovelImage().compress(Bitmap.CompressFormat.PNG, 100, fos);
        }catch (IOException e) {
            e.printStackTrace();
        }

        values.put("novel_image", n.getNovelName() + "_" + n.getSource() + "_" + "image.png");

        result = db.insert("Novels", null, values);

        if(result > -1){
            ArrayList<NovelCleaner> cleaners = getCleanersTypeOneAndTwo();
            for(NovelCleaner cleaner : cleaners){
                values = new ContentValues();

                values.put("cleaner_id", cleaner.getCleanerId());
                values.put("novel_name", n.getNovelName());
                values.put("novel_source", n.getSource());
                if(cleaner.isActive()){
                    values.put("isActive", 1);
                }else{
                    values.put("isActive", 0);
                }

                //result = db.insert("CleanerConnection", null, values);
            }
        }

        db.close();

        return result;
    }

    public synchronized long insertEpub(BookDetails b){
        ContentValues values;
        long result;

        db = database.getWritableDatabase();
        values = new ContentValues();

        values.put("last_readed", new Date().getTime());//last_readed
        values.put("on_library", 0);
        values.put("novel_type", 2);
        values.put("readerViewType", 1);
        values.put("last_page_searched", 0);
        values.put("novel_name", b.getBookName());
        values.put("novel_author", b.getBookAuthor());
        values.put("novel_description", "");
        values.put("novel_source", b.getBookPublisher());
        values.put("order_type", "DSC");
        values.put("novel_link", "epub");
        values.put("status", 3);
        values.put("finished_loading", 1);

        try (FileOutputStream fos = ctx.openFileOutput(b.getBookName() + "_epub_image.png", Context.MODE_PRIVATE)) {
            b.getBookCover().compress(Bitmap.CompressFormat.PNG, 100, fos);
        }catch (IOException e) {
            e.printStackTrace();
        }

        values.put("novel_image", b.getBookName() + "_epub_image.png");

        result = db.insert("Novels", null, values);

        //If book is stored, then add chapters
        if(result > -1){
            b.setId((int) result);

            for(int i = 0; i < b.getChapterList().size(); i ++){
                EpubChapter currentChapter = b.getChapterList().get(i);

                values = new ContentValues();

                values.put("novel_name", b.getBookName());
                values.put("novel_source", b.getBookPublisher());
                values.put("source_id", i);
                values.put("chapter_name", currentChapter.getChapterName());
                values.put("chapter_link", "epub");
                values.put("chapter_content", currentChapter.getChapterContent());
                values.put("raw_chapter", currentChapter.getRawChapterContent());
                values.put("readed", "no");
                values.put("status", "yes");

                result = db.insert("Chapters", null, values);

                if(result !=  -1){
                    currentChapter.setId((int) result);
                }
            }
        }

        db.close();

        return result;
    }

    public synchronized boolean checkIfEpubAlreadyExists(BookDetails b){
        Cursor result;
        NovelDetails novel = new NovelDetails();

        db = database.getReadableDatabase();

        String query = "SELECT * FROM Novels WHERE novel_name=? AND novel_source=?";
        result = db.rawQuery(query, new String[]{b.getBookName(), b.getBookPublisher()});
        if(result.getCount() > 0){
            db.close();
            return true;
        }

        db.close();
        return false;
    }

    public synchronized long finishedLoading(String novelName, String novelSource, int page){
        ContentValues values = new ContentValues();
        long result;

        db = database.getWritableDatabase();

        values.put("finished_loading", 1);
        values.put("last_page_searched", page);
        result = db.update("Novels", values, "novel_name=? AND novel_source=? ", new String[]{novelName, novelSource});

        db.close();

        return result;
    }

    public synchronized long putOnLibrary(String novelName, String novelSource){
        ContentValues values = new ContentValues();
        long result;

        db = database.getWritableDatabase();


        values.put("on_library", 1);
        result = db.update("Novels", values, "novel_name=? AND novel_source=? ", new String[]{novelName, novelSource});

        db.close();

        return result;
    }

    public synchronized long changeReaderViewType(String novelName, String novelSource, int newReaderViewType){
        ContentValues values = new ContentValues();
        long result;

        db = database.getWritableDatabase();


        values.put("readerViewType", newReaderViewType);
        result = db.update("Novels", values, "novel_name=? AND novel_source=? ", new String[]{novelName, novelSource});

        db.close();

        return result;
    }

    public synchronized String insertChapters(String novelName, String novelSource, ChapterIndex index) {
        ContentValues values;
        long result;

        db = database.getWritableDatabase();


        values = new ContentValues();

        values.put("novel_name", novelName);
        values.put("novel_source", novelSource);
        values.put("source_id", index.getSourceId());
        values.put("chapter_name", index.getChapterName());
        values.put("chapter_link", index.getChapterLink());
        values.put("chapter_content", "");
        values.put("raw_chapter", "");
        values.put("readed", "no");
        values.put("status", ChapterStatus.EMPTY.getNumVal());

        result = db.insert("Chapters", null, values);
        db.close();

        if(result ==  -1){
            return "Error on insert";
        }else{
            return "Sucess";
        }

    }

    public synchronized void updateChapters(String novelName, String novelSource, ArrayList<ChapterIndex> c){
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

                db.close();
                insertChapters(novelName, novelSource, c.get(i));
            } else {
                //Update

                values.put("source_id", c.get(i).getSourceId());
                db.update("Chapters", values, "id=?", new String[]{String.valueOf(c.get(i).getId())});
            }
            db.close();
        }
    }

    public synchronized void updateChapters(String novelName, String novelSource, ArrayList<ChapterIndex> c, int page){
        updateLastPageSearched(novelName, novelSource, page);
        updateChapters(novelName, novelSource, c);
    }

    public synchronized void updateLastPageSearched(String novelName, String novelSource, int page){
        db = database.getReadableDatabase();

        ContentValues values = new ContentValues();
        Cursor cursor;

        values.put("last_page_searched", page);
        long result = db.update("Novels", values, "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});

        db.close();
    }

    public synchronized void deleteNovel(String novelName, String novelSource){
        db = database.getWritableDatabase();

        db.beginTransaction();

        try {
            long result = db.delete("Chapters", "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});
            if(result >= 0){
                result = db.delete("CleanerConnection", "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});
            }

            if(result >=  0){
                long result2 = db.delete("Novels", "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});

                if(result2 >= 0){
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
        }

        db.close();
    }

    public synchronized ArrayList<NovelDetails> selectAllNovels(){
        Cursor result;
        ArrayList<NovelDetails> novelDetailsArr = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Novels WHERE on_library=1 ORDER BY last_readed DESC";
        result = db.rawQuery(query, null);

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                NovelDetails novelDetails = new NovelDetails();

                novelDetails.setDb_id(result.getInt(result.getColumnIndexOrThrow("id")));
                novelDetails.setNovelAuthor(result.getString(result.getColumnIndexOrThrow("novel_author")));
                novelDetails.setNovelName(result.getString(result.getColumnIndexOrThrow("novel_name")));
                novelDetails.setNovelDescription(result.getString(result.getColumnIndexOrThrow("novel_description")));
                novelDetails.setSource(result.getString(result.getColumnIndexOrThrow("novel_source")));
                novelDetails.setNovelLink(result.getString(result.getColumnIndexOrThrow("novel_link")));
                novelDetails.setOrderType(result.getString(result.getColumnIndexOrThrow("order_type")));
                novelDetails.setReaderViewType(result.getInt(result.getColumnIndexOrThrow("readerViewType")));
                novelDetails.setLastPageSearched(result.getInt(result.getColumnIndexOrThrow("last_page_searched")));
                novelDetails.setNovelType(result.getInt(result.getColumnIndexOrThrow("novel_type")));
                novelDetails.setNovelImageLink(result.getString(result.getColumnIndexOrThrow("novel_image")));


/*                String filePath = result.getString(result.getColumnIndexOrThrow("novel_image"));
                File mSaveBit = new File(ctx.getFilesDir(), filePath);;
                String imagePath = mSaveBit.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                novelDetails.setNovelImage(bitmap);*/

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

    public synchronized ArrayList<NovelDetails> selectOnGoingNovels(){
        Cursor result;
        ArrayList<NovelDetails> novelDetailsArr = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Novels WHERE on_library=1 AND status=1 ORDER BY last_readed DESC";
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
                novelDetails.setOrderType(result.getString(result.getColumnIndexOrThrow("order_type")));
                novelDetails.setStatus(result.getInt(result.getColumnIndexOrThrow("status")));
                novelDetails.setReaderViewType(result.getInt(result.getColumnIndexOrThrow("readerViewType")));
                novelDetails.setLastPageSearched(result.getInt(result.getColumnIndexOrThrow("last_page_searched")));
                novelDetails.setNovelType(result.getInt(result.getColumnIndexOrThrow("novel_type")));

/*                String filePath = result.getString(result.getColumnIndexOrThrow("novel_image"));
                File mSaveBit = new File(ctx.getFilesDir(), filePath);;
                String imagePath = mSaveBit.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                novelDetails.setNovelImage(bitmap);*/

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

    public synchronized ArrayList<NovelDetails> selectAllNovelsFromSource(String source){
        Cursor result;
        ArrayList<NovelDetails> novelDetailsArr = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Novels WHERE on_library=1 AND novel_source=? ORDER BY last_readed DESC";
        result = db.rawQuery(query, new String[]{source});

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                NovelDetails novelDetails = new NovelDetails();

                novelDetails.setNovelAuthor(result.getString(result.getColumnIndexOrThrow("novel_author")));
                novelDetails.setNovelName(result.getString(result.getColumnIndexOrThrow("novel_name")));
                novelDetails.setNovelDescription(result.getString(result.getColumnIndexOrThrow("novel_description")));
                novelDetails.setSource(result.getString(result.getColumnIndexOrThrow("novel_source")));
                novelDetails.setNovelLink(result.getString(result.getColumnIndexOrThrow("novel_link")));
                novelDetails.setOrderType(result.getString(result.getColumnIndexOrThrow("order_type")));
                novelDetails.setStatus(result.getInt(result.getColumnIndexOrThrow("status")));
                novelDetails.setReaderViewType(result.getInt(result.getColumnIndexOrThrow("readerViewType")));
                novelDetails.setLastPageSearched(result.getInt(result.getColumnIndexOrThrow("last_page_searched")));
                novelDetails.setNovelType(result.getInt(result.getColumnIndexOrThrow("novel_type")));

/*                String filePath = result.getString(result.getColumnIndexOrThrow("novel_image"));
                File mSaveBit = new File(ctx.getFilesDir(), filePath);;
                String imagePath = mSaveBit.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                novelDetails.setNovelImage(bitmap);*/

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

    public synchronized NovelDetails getNovel(String novelName, String novelSource){
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
            novel.setOrderType(result.getString(result.getColumnIndexOrThrow("order_type")));
            novel.setStatus(result.getInt(result.getColumnIndexOrThrow("status")));
            novel.setLastReadied(result.getLong(result.getColumnIndexOrThrow("last_readed")));
            novel.setReaderViewType(result.getInt(result.getColumnIndexOrThrow("readerViewType")));
            novel.setFinishedLoading(result.getInt(result.getColumnIndexOrThrow("finished_loading")));
            novel.setLastPageSearched(result.getInt(result.getColumnIndexOrThrow("last_page_searched")));
            novel.setNovelType(result.getInt(result.getColumnIndexOrThrow("novel_type")));


            int is_favorite = result.getInt(result.getColumnIndexOrThrow("on_library"));

            if(is_favorite == 0){
                novel.setIsFavorite("no");
            }else{
                novel.setIsFavorite("yes");
            }

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

    public synchronized NovelDetails getNovelWithNovelLink(String novelSource, String novelLink){
        Cursor result;
        NovelDetails novel = new NovelDetails();

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Novels WHERE novel_source=? AND novel_link=?";
        result = db.rawQuery(query, new String[]{novelSource, novelLink});
        if(result.getCount() > 0){
            result.moveToFirst();

            novel.setNovelAuthor(result.getString(result.getColumnIndexOrThrow("novel_author")));
            novel.setNovelName(result.getString(result.getColumnIndexOrThrow("novel_name")));
            novel.setNovelDescription(result.getString(result.getColumnIndexOrThrow("novel_description")));
            novel.setSource(result.getString(result.getColumnIndexOrThrow("novel_source")));
            novel.setNovelLink(result.getString(result.getColumnIndexOrThrow("novel_link")));
            novel.setOrderType(result.getString(result.getColumnIndexOrThrow("order_type")));
            novel.setStatus(result.getInt(result.getColumnIndexOrThrow("status")));
            novel.setLastReadied(result.getLong(result.getColumnIndexOrThrow("last_readed")));
            novel.setReaderViewType(result.getInt(result.getColumnIndexOrThrow("readerViewType")));
            novel.setLastPageSearched(result.getInt(result.getColumnIndexOrThrow("last_page_searched")));
            novel.setNovelType(result.getInt(result.getColumnIndexOrThrow("novel_type")));

            int is_favorite = result.getInt(result.getColumnIndexOrThrow("on_library"));

            if(is_favorite == 0){
                novel.setIsFavorite("no");
            }else{
                novel.setIsFavorite("yes");
            }

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

    public synchronized ArrayList<ChapterIndex> getChaptersFromANovel(String novelName, String novelSource){
        Cursor result;
        ArrayList<ChapterIndex> chapterIndexes = new ArrayList<>();

        db = database.getReadableDatabase();

        String query = "SELECT id, chapter_link, chapter_name, status, readed, source_id, last_page_readed FROM Chapters WHERE Chapters.novel_name=? AND Chapters.novel_source=? ORDER BY source_id ASC";
        result = db.rawQuery(query, new String[]{novelName, novelSource});

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                ChapterIndex c = new ChapterIndex();

                c.setId(result.getInt(result.getColumnIndexOrThrow("id")));
                c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
                c.setChapterLink(result.getString(result.getColumnIndexOrThrow("chapter_link")));

                ChapterStatus status = ChapterStatus.EMPTY;
                c.setStatus(status.setValue(result.getInt(result.getColumnIndexOrThrow("status"))));

                c.setReaded(result.getString(result.getColumnIndexOrThrow("readed")));
                c.setLastPageReaded(result.getInt(result.getColumnIndexOrThrow("last_page_readed")));

                c.setSourceId(result.getInt(result.getColumnIndexOrThrow("source_id")));

                chapterIndexes.add(c);
            }while (result.moveToNext());
        }

        db.close();

        return chapterIndexes;
    }

    public synchronized boolean updateLastPageReaded(int chapterId, int page){
        long result;
        ContentValues values = new ContentValues();

        db = database.getReadableDatabase();

        values.put("last_page_readed", page);

        result = db.update("Chapters", values, "id=?", new String[]{String.valueOf(chapterId)});

        if(result ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public synchronized ArrayList<ChapterInDownload>  getDownloadingChapters(){
        Cursor result;
        ArrayList<ChapterInDownload> chapterIndexes = new ArrayList<>();

        db = database.getReadableDatabase();
        String query = "" +
                "SELECT " +
                "Chapters.id, Chapters.chapter_name,Chapters.novel_name, Novels.novel_image " +
                "FROM Chapters " +
                "INNER JOIN Novels on Novels.novel_name = Chapters.novel_name AND Novels.novel_source = Chapters.novel_source " +
                "WHERE Chapters.status=?";

        result = db.rawQuery(query, new String[]{String.valueOf(ChapterStatus.DOWNLOADING.getNumVal())});

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

    public synchronized boolean putChapterOnDownload(int id){
        long result;
        ContentValues values = new ContentValues();;

        db = database.getReadableDatabase();

        values.put("status", "downloading");

        result = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        if(result ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public synchronized boolean putChapterOnDownload(String novelName, String source, String chapterLink){
        long result;
        ContentValues values = new ContentValues();;

        int id = getChapterId(chapterLink);

        return putChapterOnDownload(novelName, source, id);
    }

    public synchronized boolean putChapterOnDownload(String novelName, String novelSource, int id){
        long result;
        ContentValues values = new ContentValues();;

        db = database.getReadableDatabase();

        values.put("status", ChapterStatus.DOWNLOADING.getNumVal());
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

    public synchronized boolean PutMultipleChaptersToDownload(String novelName, String novelSource, ArrayList<ChapterIndex> chapters){
        boolean haveAnChapterToDownload = false;

        for(ChapterIndex c : chapters){
            db = database.getReadableDatabase();
            String query = "SELECT status FROM Chapters WHERE id=" + c.getId() + " ";
            Cursor result = db.rawQuery(query, new String[]{});

            if(result.getCount() > 0) {
                result.moveToFirst();

                ChapterStatus status = ChapterStatus.EMPTY;
                status = status.setValue(result.getInt(result.getColumnIndexOrThrow("status")));

                if(status == ChapterStatus.EMPTY){
                   putChapterOnDownload(novelName, novelSource, c.getChapterLink());

                   haveAnChapterToDownload = true;
                }
            }

            db.close();
        }

        return haveAnChapterToDownload;
    }

    public synchronized void DeleteMultipleChaptersContent(ArrayList<ChapterIndex> chapters){
        db = database.getReadableDatabase();

        for(ChapterIndex c : chapters){
            ContentValues values = new ContentValues();

            values.put("chapter_content", "");
            values.put("status", "no");

            db.update("Chapters", values, "id=?", new String[]{String.valueOf(c.getId())});
        }

        db.close();
    }

    public synchronized boolean setChapterAsReaded(int id){
        Cursor result;
        ContentValues values = new ContentValues();

        db = database.getReadableDatabase();

        String query = "SELECT * FROM Chapters WHERE Chapters.id=? ";
        result = db.rawQuery(query, new String[]{String.valueOf(id)});

        if(result.getCount() > 0){
            result.moveToFirst();

            String novelName = result.getString(result.getColumnIndexOrThrow("novel_name"));
            String novelSource = result.getString(result.getColumnIndexOrThrow("novel_source"));

            values.put("last_readed", new Date().getTime());//last_readed
            long result2 = db.update("Novels", values, "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});

            if(result2 == -1){
                db.close();
                return false;
            }
        }else {
            db.close();
            return false;
        }

        values = new ContentValues();

        values.put("readed", "yes");
        long result2 = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        db.close();

        if(result2 ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public synchronized boolean setChapterAsUnReaded(int id){
        Cursor result;
        ContentValues values = new ContentValues();

        db = database.getReadableDatabase();

        String query = "SELECT * FROM Chapters WHERE Chapters.id=? ";
        result = db.rawQuery(query, new String[]{String.valueOf(id)});

        if(result.getCount() > 0){
            result.moveToFirst();

            String novelName = result.getString(result.getColumnIndexOrThrow("novel_name"));
            String novelSource = result.getString(result.getColumnIndexOrThrow("novel_source"));

            values.put("last_readed", new Date().getTime());//last_readed
            long result2 = db.update("Novels", values, "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});

            if(result2 == -1){
                db.close();
                return false;
            }
        }else {
            db.close();
            return false;
        }

        values = new ContentValues();

        values.put("readed", "no");
        long result2 = db.update("Chapters", values, "id=?", new String[]{String.valueOf(id)});

        db.close();

        if(result2 ==  -1){
            return false;
        }else{
            return true;
        }
    }

    public synchronized ArrayList<DownloaderClass> getDownloadingNovels(){
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
                "WHERE Chapters.status=?" +
                "ORDER BY DownloadQueue.id ASC";

        result = db.rawQuery(query, new String[]{String.valueOf(ChapterStatus.DOWNLOADING.getNumVal())});

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                ChapterIndex c = new ChapterIndex();

                c.setId(result.getInt(result.getColumnIndexOrThrow("id")));
                c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
                c.setChapterLink(result.getString(result.getColumnIndexOrThrow("chapter_link")));
                c.setStatus(ChapterStatus.DOWNLOADING);

                String novelName = result.getString(result.getColumnIndexOrThrow("novel_name"));
                String novelSource = result.getString(result.getColumnIndexOrThrow("novel_source"));

                downloaderClasses.add(new DownloaderClass(novelName, novelSource, c));

            }while (result.moveToNext());
        }


        return downloaderClasses;
    }

    public synchronized ChapterContent getChapter(int id){
        Cursor result;

        db = database.getReadableDatabase();
        String query = "SELECT * FROM Chapters WHERE Chapters.id=? ";
        result = db.rawQuery(query, new String[]{String.valueOf(id)});

        if(result.getCount() > 0){
            result.moveToFirst();

            ChapterContent c = new ChapterContent();

            c.setChapterContent(result.getString(result.getColumnIndexOrThrow("chapter_content")));
            c.setRawChapter(result.getString(result.getColumnIndexOrThrow("raw_chapter")));
            c.setChapterName(result.getString(result.getColumnIndexOrThrow("chapter_name")));
            c.setChapterLink(result.getString(result.getColumnIndexOrThrow("chapter_link")));

            db.close();
            return c;
        }

        db.close();
        return null;
    }

    public synchronized int getChapterId(String chapterLink){
        Cursor result;

        db = database.getReadableDatabase();
        String query = "SELECT id FROM Chapters WHERE Chapters.chapter_link=? ";
        result = db.rawQuery(query, new String[]{chapterLink});

        if(result.getCount() > 0){
            result.moveToFirst();

            db.close();
            return result.getInt(result.getColumnIndexOrThrow("id"));

        }

        db.close();
        return -1;
    }

    public synchronized boolean setChapterContent(int id, String chapterContent, String rawChapter){
        long result;
        db = database.getReadableDatabase();

        ContentValues values = new ContentValues();;
        values.put("chapter_content", chapterContent);
        values.put("raw_chapter", rawChapter);
        values.put("status", ChapterStatus.DOWNLOADED.getNumVal());

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

    public synchronized boolean setChapterError(int id){
        long result;
        db = database.getReadableDatabase();

        ContentValues values = new ContentValues();;
        values.put("status", ChapterStatus.ERROR.getNumVal());

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

    public synchronized boolean setChaptersReadied(String novelName, String novelSource, String _query){
        Cursor result;

        db = database.getReadableDatabase();

        String query = "UPDATE Chapters"
                + " SET readed = 'yes'"
                + " WHERE  novel_name = '" + novelName + "' "
                + "AND novel_source = '" + novelSource + "' "
                + "" + _query + " AND TRUE";

        result = db.rawQuery(query, new String[]{});

        result.moveToFirst();
        result.close();
        db.close();

        return true;
    }

    public synchronized boolean SetAntecedentChaptersAsReadied(String novelName, String novelSource, int lowerSourceId){
        ContentValues values = new ContentValues();
        Cursor result;

        db = database.getReadableDatabase();

        values.put("last_readed", new Date().getTime());//last_readed
        long result2 = db.update("Novels", values, "novel_name=? AND novel_source=?", new String[]{novelName, novelSource});

        if(result2 == -1){
            db.close();
            return false;
        }

        String query = "UPDATE Chapters"
                + " SET readed = 'yes'"
                + " WHERE  novel_name = '" + novelName + "' "
                + "AND novel_source = '" + novelSource + "' "
                + " AND source_id < " + lowerSourceId;

        result = db.rawQuery(query, new String[]{});

        result.moveToFirst();
        result.close();
        db.close();

        return true;
    }

    public synchronized void UpdateOrderType(String novelName, String novelSource, String orderType){
        long result;
        db = database.getReadableDatabase();

        ContentValues values = new ContentValues();;
        values.put("order_type", orderType);

        result = db.update("Novels", values, "novel_name=? AND novel_source=? ", new String[]{novelName, novelSource});

        db.close();
    }

    public synchronized long CreateCleaner(NovelCleaner cleaner){
        ContentValues values;
        long result;

        db = database.getWritableDatabase();
        values = new ContentValues();

        values.put("type", cleaner.getType());
        values.put("cleaner_name", cleaner.getName());
        values.put("flag", cleaner.getFlag());
        values.put("replacement", cleaner.getReplacement());

        result = db.insert("Cleaners", null, values);
        db.close();

        return result;
    }

    public synchronized ArrayList<NovelCleaner> CreateCleanerConnectionsAny(NovelCleaner cleaner){
        ContentValues values;
        long result;
        long cleanerID = CreateCleaner(cleaner);

        ArrayList<NovelCleaner> tempNovelCleaners = new ArrayList<>();

        if(cleanerID > -1){
            ArrayList<NovelDetails> allNovels = selectAllNovels();

            db = database.getWritableDatabase();

            for(NovelDetails n : allNovels){
                NovelCleaner tempCleaner = new NovelCleaner(
                        cleaner.getName(),
                        cleaner.getFlag(),
                        cleaner.getReplacement(),
                        cleaner.getType());

                tempCleaner.setCleanerId((int) cleanerID);

                values = new ContentValues();

                values.put("cleaner_id", cleanerID);
                values.put("novel_name", n.getNovelName());
                values.put("novel_source", n.getSource());
                if(cleaner.isActive()){
                    values.put("isActive", 1);
                }else{
                    values.put("isActive", 0);

                }

                result = db.insert("CleanerConnection", null, values);

                tempCleaner.setConnectionId((int) result);
            }

            db.close();
        }

        return tempNovelCleaners;
    }

    public synchronized ArrayList<NovelCleaner> CreateCleanerConnectionsSource(NovelCleaner cleaner, String novelSource){
        ArrayList<NovelCleaner> tempNovelCleaners = new ArrayList<>();
        ContentValues values;
        long result;
        long cleanerID = CreateCleaner(cleaner);

        if(cleanerID > -1){
            ArrayList<NovelDetails> allNovels = selectAllNovelsFromSource(novelSource);

            db = database.getWritableDatabase();

            for(NovelDetails n : allNovels){
                NovelCleaner tempCleaner = new NovelCleaner(
                        cleaner.getName(),
                        cleaner.getFlag(),
                        cleaner.getReplacement(),
                        cleaner.getType());

                tempCleaner.setCleanerId((int) cleanerID);

                values = new ContentValues();

                values.put("cleaner_id", cleanerID);
                values.put("novel_name", n.getNovelName());
                values.put("novel_source", n.getSource());
                if(cleaner.isActive()){
                    values.put("isActive", 1);
                }else{
                    values.put("isActive", 0);
                }

                result = db.insert("CleanerConnection", null, values);
                tempCleaner.setConnectionId((int) result);
            }

            db.close();
        }

        return tempNovelCleaners;
    }

    public synchronized NovelCleaner CreateCleanerConnectionsNovel(NovelCleaner cleaner, String novelName, String novelSource){
        ContentValues values;
        long result;
        long cleanerID = CreateCleaner(cleaner);

        NovelCleaner tempCleaner = new NovelCleaner(
                cleaner.getName(),
                cleaner.getFlag(),
                cleaner.getReplacement(),
                cleaner.getType());

        tempCleaner.setCleanerId((int) cleanerID);

        if(cleanerID > -1){
            db = database.getWritableDatabase();

            values = new ContentValues();

            values.put("cleaner_id", cleanerID);
            values.put("novel_name", novelName);
            values.put("novel_source", novelSource);
            if(cleaner.isActive()){
                values.put("isActive", 1);
            }else{
                values.put("isActive", 0);
            }

            result = db.insert("CleanerConnection", null, values);

            tempCleaner.setConnectionId((int) result);

            db.close();
        }

        return tempCleaner;
    }

    public synchronized ArrayList<NovelCleaner> getCleanerConnections(String novelName, String novelSource){
        Cursor result;
        ArrayList<NovelCleaner> cleaners = new ArrayList<>();

        db = database.getReadableDatabase();

        String query = "" +
                "SELECT CleanerConnection.*, Cleaners.type, Cleaners.cleaner_name, Cleaners.flag, Cleaners.replacement " +
                "FROM Cleaners " +
                "INNER JOIN CleanerConnection ON Cleaners.id = CleanerConnection.cleaner_id " +
                "WHERE CleanerConnection.novel_name=? AND CleanerConnection.novel_source=?";
        result = db.rawQuery(query, new String[]{novelName, novelSource});

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                NovelCleaner cleaner = new NovelCleaner();

                cleaner.setName(result.getString(result.getColumnIndexOrThrow("cleaner_name")));
                cleaner.setFlag(result.getString(result.getColumnIndexOrThrow("flag")));
                cleaner.setReplacement(result.getString(result.getColumnIndexOrThrow("replacement")));
                cleaner.setType(result.getInt(result.getColumnIndexOrThrow("type")));
                cleaner.setConnectionId(result.getInt(result.getColumnIndexOrThrow("id")));
                cleaner.setCleanerId(result.getInt(result.getColumnIndexOrThrow("cleaner_id")));

                int isActive = result.getInt(result.getColumnIndexOrThrow("isActive"));

                if(isActive == 1){
                    cleaner.setActive(true);
                }else{
                    cleaner.setActive(false);
                }

                cleaners.add(cleaner);
            }while (result.moveToNext());
        }

        db.close();

        return cleaners;
    }

    public synchronized ArrayList<NovelCleaner> getCleanersTypeOneAndTwo(){
        Cursor result;
        ArrayList<NovelCleaner> cleaners = new ArrayList<>();

        db = database.getReadableDatabase();

        String query = "" +
                "SELECT * " +
                "FROM Cleaners " +
                "WHERE Cleaners.type=1 OR Cleaners.type=2 ";
        result = db.rawQuery(query, null);

        if(result.getCount() > 0){
            result.moveToFirst();

            do {
                NovelCleaner cleaner = new NovelCleaner();

                cleaner.setName(result.getString(result.getColumnIndexOrThrow("cleaner_name")));
                cleaner.setFlag(result.getString(result.getColumnIndexOrThrow("flag")));
                cleaner.setReplacement(result.getString(result.getColumnIndexOrThrow("replacement")));
                cleaner.setType(result.getInt(result.getColumnIndexOrThrow("type")));
                cleaner.setCleanerId(result.getInt(result.getColumnIndexOrThrow("id")));

                cleaner.setActive(false);

                cleaners.add(cleaner);
            }while (result.moveToNext());
        }

        db.close();

        return cleaners;
    }


    public synchronized void deleteCleaner(int cleaner_id){
        db = database.getWritableDatabase();

        db.beginTransaction();

        try {
            long result = db.delete("CleanerConnection", "cleaner_id=?", new String[]{String.valueOf(cleaner_id)});
            if(result >=  0){
                long result2 = db.delete("Cleaners", "id=?", new String[]{String.valueOf(cleaner_id)});

                if(result2 >= 0){
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
        }

        db.close();
    }

    public synchronized void ChangeIsActiveConnection(int connection_id, boolean isActive){

        ContentValues values = new ContentValues();
        long result;

        db = database.getWritableDatabase();


        if(isActive){
            values.put("isActive", 1);
        }else{
            values.put("isActive", 0);
        }

        result = db.update("CleanerConnection", values, "id=? ", new String[]{String.valueOf(connection_id)});

        db.close();
    }

    public synchronized void EditCleaner(NovelCleaner cleaner){
        ContentValues values = new ContentValues();
        long result;

        db = database.getWritableDatabase();

        values.put("cleaner_name", cleaner.getName());
        values.put("flag", cleaner.getFlag());
        values.put("replacement", cleaner.getReplacement());


        result = db.update("Cleaners", values, "id=? ", new String[]{String.valueOf(cleaner.getCleanerId())});

        db.close();
    }
}
