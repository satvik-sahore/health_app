package com.mcu.diashield;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HealthData.db";
    private static final int DATABASE_VERSION = 1;

    // Define your table and column names
    private static final String TABLE_HEART_RATE = "heart_rate";
    private static final String TABLE_RESPIRATORY_RATE = "respiratory_rate";
    private static final String TABLE_SYMPTOMS_RATINGS = "symptoms_ratings";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_SYMPTOM = "symptom";
    private static final String COLUMN_RATING = "rating";

    // Constructor
    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        String createHeartRateTable = "CREATE TABLE " + TABLE_HEART_RATE + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_VALUE + " INTEGER)";

        String createRespiratoryRateTable = "CREATE TABLE " + TABLE_RESPIRATORY_RATE + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_VALUE + " INTEGER)";

        String createSymptomsTable = "CREATE TABLE " + TABLE_SYMPTOMS_RATINGS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SYMPTOM + " TEXT, "
                + COLUMN_RATING + " REAL)";

        db.execSQL(createHeartRateTable);
        db.execSQL(createRespiratoryRateTable);
        db.execSQL(createSymptomsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades here
    }

    public void insertHeartRate(int rate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, rate);
        db.insert(TABLE_HEART_RATE, null, values);
        db.close();
    }

    public void insertRespiratoryRate(int rate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, rate);
        db.insert(TABLE_RESPIRATORY_RATE, null, values);
        db.close();
    }
}
