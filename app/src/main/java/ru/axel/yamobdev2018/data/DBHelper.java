package ru.axel.yamobdev2018.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "images.db";
    private static final int VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_QUERIES = String.format(
                "CREATE TABLE %s (" +
                        "%s INTEGER PRIMARY KEY, " +
                        "%s TEXT, " +
                        "%s DATETIME DEFAULT (datetime('now','localtime')), " +
                        "%s INTEGER DEFAULT 1);",
                ImageContract.QueryEntry.TABLE_NAME,
                ImageContract.QueryEntry._ID,
                ImageContract.QueryEntry.COLUMN_KEYWORD,
                ImageContract.QueryEntry.COLUMN_DATE,
                ImageContract.QueryEntry.COLUMN_PAGE
        );

        final String CREATE_TABLE_IMAGES = String.format(
                "CREATE TABLE %s (" +
                        "%s INTEGER PRIMARY KEY, " +
                        "%s INTEGER, " +
                        "%s TEXT);",
                ImageContract.ImageEntry.TABLE_NAME,
                ImageContract.ImageEntry._ID,
                ImageContract.ImageEntry.COLUMN_QUERY_ID,
                ImageContract.ImageEntry.COLUMN_URL
        );

        db.execSQL(CREATE_TABLE_QUERIES);
        db.execSQL(CREATE_TABLE_IMAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ImageContract.ImageEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImageContract.QueryEntry.TABLE_NAME);
        onCreate(db);
    }
}
