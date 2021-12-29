package com.example.mobileproject.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class CreateDB extends SQLiteOpenHelper {
    private static final String db_name = "reader_db";
    private static final String Table1 = "Novels";
    private static final String Table2 = "Chapters";
    private static final String Table3 = "DownloadQueue";

    private static CreateDB sInstance;

    public static synchronized CreateDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CreateDB(context.getApplicationContext());
        }
        return sInstance;
    }

    public CreateDB(@Nullable Context context) {
        super(context, db_name, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+ Table1 +" ("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "last_readed" + " integer,"
                + "on_library" + " integer,"
                + "novel_name" + " text,"
                + "novel_author" + " text,"
                + "novel_description" + " text,"
                + "novel_link" + " text,"
                + "novel_source" + " text,"
                + "novel_image" + " text,"
                + "order_type " + " text,"
                + "status " + " integer,"
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
                + "chapter_content" + " text,"
                + "readed" + " text,"
                + "downloaded" + " text,"
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Table1);
        db.execSQL("DROP TABLE IF EXISTS " + Table2);
        db.execSQL("DROP TABLE IF EXISTS " + Table3);

        onCreate(db);
    }
}
