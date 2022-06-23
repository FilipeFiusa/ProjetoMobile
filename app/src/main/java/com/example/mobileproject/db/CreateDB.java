package com.example.mobileproject.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.mobileproject.model.ChapterStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateDB extends SQLiteOpenHelper {
    private static final String db_name = "reader_db";
    private static final String Table1 = "Novels";
    private static final String Table2 = "Chapters";
    private static final String Table3 = "DownloadQueue";
    private static final String Table4 = "Cleaners";
    private static final String Table5 = "CleanerConnection";

    private static CreateDB sInstance;

    public static synchronized CreateDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CreateDB(context.getApplicationContext());
        }
        return sInstance;
    }

    public CreateDB(@Nullable Context context) {
        super(context, db_name, null, 9);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+ Table1 +" ("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "last_readed" + " integer,"
                + "on_library" + " integer,"
                + "readerViewType" + " integer,"
                + "novel_type" + " integer,"
                + "last_page_searched" + " integer,"
                + "novel_name" + " text,"
                + "novel_author" + " text,"
                + "novel_description" + " text,"
                + "novel_link" + " text,"
                + "novel_source" + " text,"
                + "novel_image" + " text,"
                + "order_type " + " text,"
                + "status " + " integer,"
                + "finished_loading " + " integer,"
                + "UNIQUE(novel_name, novel_source)"
                +");";

        db.execSQL(sql);

        sql = "CREATE TABLE "+ Table2 + " ("
                + "id integer primary key autoincrement,"
                + "source_id integer,"
                + "novel_name text,"
                + "novel_source text,"
                + "chapter_name" + " text,"
                + "chapter_link" + " text,"
                + "raw_chapter" + " text,"
                + "chapter_content" + " text,"
                + "readed" + " text,"
                //+ "downloaded" + " text,"
                + "status" + " integer DEFAULT 1,"
                + "FOREIGN KEY(novel_name) REFERENCES Novels(novel_name),"
                + "FOREIGN KEY(novel_source) REFERENCES Novels(novel_source)"
                +")";

        db.execSQL(sql);

        sql = "CREATE TABLE "+ Table3 +" ("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "novel_name text,"
                + "novel_source text,"
                + "chapter_id" + " INTEGER,"
                + "FOREIGN KEY(novel_name) REFERENCES Novels(novel_name),"
                + "FOREIGN KEY(novel_source) REFERENCES Novels(novel_source),"
                + "FOREIGN KEY(chapter_id) REFERENCES Chapters(id)"
                +");";

        db.execSQL(sql);

        sql = "CREATE TABLE "+ Table4 +" ("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "type" + " INTEGER NOT NULL,"
                + "cleaner_name text NOT NULL,"
                + "flag text NOT NULL,"
                + "replacement text NOT NULL"
                +");";

        db.execSQL(sql);

        sql = "CREATE TABLE "+ Table5 +" ("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "cleaner_id" + " INTEGER NOT NULL,"
                + "novel_name text,"
                + "novel_source text,"
                + "isActive int NOT NULL,"
                + "FOREIGN KEY(cleaner_id) REFERENCES Cleaners(id),"
                + "FOREIGN KEY(novel_name) REFERENCES Novels(novel_name),"
                + "FOREIGN KEY(novel_source) REFERENCES Novels(novel_source)"
                +");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion <= 9 && newVersion > oldVersion){
            System.out.println("Salvando dados");
            //db.execSQL("ALTER TABLE " + Table2 + " ADD COLUMN status INTEGER DEFAULT 1");

            ArrayList<tempChap> chapters = new ArrayList<>();
            String query = "SELECT id, downloaded FROM Chapters";
            Cursor result = db.rawQuery(query, new String[]{});

            if(result.getCount() > 0){
                result.moveToFirst();

                do {
                    int id = result.getInt(result.getColumnIndexOrThrow("id"));

                    String status = result.getString(result.getColumnIndexOrThrow("downloaded"));

                    if (status.equals("no")){
                        chapters.add(new tempChap(id, ChapterStatus.EMPTY));
                    }else if (status.equals("downloading")){
                        chapters.add(new tempChap(id, ChapterStatus.DOWNLOADING));
                    }else if (status.equals("yes")){
                        chapters.add(new tempChap(id, ChapterStatus.DOWNLOADED));
                    }else {
                        chapters.add(new tempChap(id, ChapterStatus.ERROR));
                    }
                }while (result.moveToNext());
            }

            System.out.println("Dados salvos. Quantidade: " + chapters.size());
            System.out.println("Iniciando Transaction");

            String temporaryTable = "CREATE TEMPORARY TABLE chapters_backup ("
                    + "id integer primary key autoincrement,"
                    + "source_id integer,"
                    + "novel_name text,"
                    + "novel_source text,"
                    + "chapter_name" + " text,"
                    + "chapter_link" + " text,"
                    + "raw_chapter" + " text,"
                    + "chapter_content" + " text,"
                    + "readed" + " text,"
                    //+ "downloaded" + " text,"
                    //+ "status" + " integer,"
                    + "FOREIGN KEY(novel_name) REFERENCES Novels(novel_name),"
                    + "FOREIGN KEY(novel_source) REFERENCES Novels(novel_source)"
                    +")";

            String createTableAgain = "CREATE TABLE "+ Table2 + " ("
                    + "id integer primary key autoincrement,"
                    + "source_id integer,"
                    + "novel_name text,"
                    + "novel_source text,"
                    + "chapter_name" + " text,"
                    + "chapter_link" + " text,"
                    + "raw_chapter" + " text,"
                    + "chapter_content" + " text,"
                    + "readed" + " text,"
                    //+ "downloaded" + " text,"
                    + "status" + " integer DEFAULT 1,"
                    + "FOREIGN KEY(novel_name) REFERENCES Novels(novel_name),"
                    + "FOREIGN KEY(novel_source) REFERENCES Novels(novel_source)"
                    +")";

            db.execSQL("BEGIN TRANSACTION");
            db.execSQL(temporaryTable);
            db.execSQL("INSERT INTO chapters_backup " +
                    "SELECT id, source_id, novel_name, novel_source, chapter_name, chapter_link, raw_chapter, chapter_content, readed " +
                    "FROM Chapters");
            db.execSQL("DROP TABLE Chapters");
            db.execSQL(createTableAgain);
            db.execSQL("INSERT INTO Chapters " +
                    "SELECT id, source_id, novel_name, novel_source, chapter_name, chapter_link, raw_chapter, chapter_content, readed, 1 " +
                    "FROM chapters_backup");
            db.execSQL("DROP TABLE chapters_backup");
            db.execSQL("COMMIT");

            System.out.println("Transaction finalizada");

            for (tempChap c : chapters){
                ContentValues values = new ContentValues();

                values.put("status", c.getStatus().getNumVal());
                db.update(Table2, values, "id=? ", new String[]{String.valueOf(c.getId())});
            }

            System.out.println("Upgrade finalizado");
        }
    }

    private static class tempChap{
        private int id;
        private ChapterStatus status;

        public tempChap(int id, ChapterStatus status) {
            this.id = id;

            this.status = status;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ChapterStatus getStatus() {
            return status;
        }

        public void setStatus(ChapterStatus status) {
            this.status = status;
        }
    }
}
