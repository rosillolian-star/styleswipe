package com.example.styleswipe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DATABASE HELPER CLASS
 * This class handles all local data storage using SQLite.
 */
public class OutfitDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "outfits.db";
    private static final int DB_VERSION = 6; // Added planned_outfits table

    public OutfitDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. USERS TABLE
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE," +
                "password TEXT)");

        // 2. ALBUMS TABLE
        db.execSQL("CREATE TABLE albums (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

        // 3. OUTFITS TABLE
        db.execSQL("CREATE TABLE outfits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER DEFAULT -1," +
                "event TEXT," +
                "location TEXT," +
                "tags TEXT," +
                "notes TEXT," +
                "imageUri TEXT," +
                "imageBitmap BLOB," +
                "date_taken DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "album_id INTEGER DEFAULT -1," +
                "is_favorite INTEGER DEFAULT 0)"); // 0 = false, 1 = true

        // 4. PLANNED OUTFITS TABLE (Calendar)
        db.execSQL("CREATE TABLE planned_outfits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "outfit_id INTEGER," +
                "date TEXT UNIQUE)"); // Store as YYYY-MM-DD
        
        // Default album
        db.execSQL("INSERT INTO albums (name) VALUES ('History')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS outfits");
            db.execSQL("DROP TABLE IF EXISTS albums");
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS planned_outfits");
            onCreate(db);
        }
    }
}
