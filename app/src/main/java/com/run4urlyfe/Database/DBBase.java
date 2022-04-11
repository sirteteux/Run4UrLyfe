package com.run4urlyfe.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBBase {

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DBBase(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public SQLiteDatabase open() {
        return database = dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return database = dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
}
