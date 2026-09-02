package com.example.styleswipe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DATABASE HELPER CLASS
 * This class handles all local data storage using SQLite.
 * It manages the creation, updates, and schema of tables for Users, Albums, and Outfits.
 */
public class OutfitDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "outfits.db";
    private static final int DB_VERSION = 4; // Incremented version to apply latest schema changes

    public OutfitDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * TABLE CREATION
     * This method runs once when the database is first created on the device.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. USERS TABLE: Stores offline account credentials
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE," + // Unique constraint prevents duplicate registrations
                "password TEXT)");

        // 2. ALBUMS TABLE: Manages style collections/folders
        db.execSQL("CREATE TABLE albums (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

        // 3. OUTFITS TABLE: The main table storing all captured fashion data
        db.execSQL("CREATE TABLE outfits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER DEFAULT -1," + // Links outfit to a specific user
                "event TEXT," +                 // Occasion name
                "location TEXT," +              // Where it was worn
                "tags TEXT," +                  // Categorization labels
                "notes TEXT," +                 // Detailed description
                "imageUri TEXT," +              // Path for gallery-picked images
                "imageBitmap BLOB," +           // Binary data for camera-captured images
                "date_taken DATETIME DEFAULT CURRENT_TIMESTAMP," + // Automatic timestamp
                "album_id INTEGER DEFAULT -1)"); // Links outfit to an album
        
        // 4. SEED DATA: Create a default "Recent" album so the app isn't empty
        db.execSQL("INSERT INTO albums (name) VALUES ('Recent')");
    }

    /**
     * DATABASE UPDATES
     * This method runs when DB_VERSION is increased. 
     * It clears old tables and recreates them with the new structure.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy: reset the app data to apply new structure
        db.execSQL("DROP TABLE IF EXISTS outfits");
        db.execSQL("DROP TABLE IF EXISTS albums");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
