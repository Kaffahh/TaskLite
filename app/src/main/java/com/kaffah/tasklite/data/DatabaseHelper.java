package com.kaffah.tasklite.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DatabaseContract.Categories.TABLE + " ("
                + DatabaseContract.Categories.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Categories.NAME + " TEXT NOT NULL COLLATE NOCASE UNIQUE, "
                + DatabaseContract.Categories.COLOR + " INTEGER NOT NULL, "
                + DatabaseContract.Categories.CREATED_AT + " INTEGER NOT NULL, "
                + DatabaseContract.Categories.UPDATED_AT + " INTEGER NOT NULL, "
                + "CHECK (length(trim(" + DatabaseContract.Categories.NAME + ")) > 0)"
                + ")");

        db.execSQL("CREATE TABLE " + DatabaseContract.Tasks.TABLE + " ("
                + DatabaseContract.Tasks.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.Tasks.TITLE + " TEXT NOT NULL, "
                + DatabaseContract.Tasks.DESCRIPTION + " TEXT NOT NULL DEFAULT '', "
                + DatabaseContract.Tasks.CATEGORY_ID + " INTEGER, "
                + DatabaseContract.Tasks.PRIORITY + " INTEGER NOT NULL DEFAULT 0, "
                + DatabaseContract.Tasks.DUE_AT + " INTEGER, "
                + DatabaseContract.Tasks.REMINDER_ENABLED + " INTEGER NOT NULL DEFAULT 0, "
                + DatabaseContract.Tasks.REMINDER_AT + " INTEGER, "
                + DatabaseContract.Tasks.IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + DatabaseContract.Tasks.COMPLETED_AT + " INTEGER, "
                + DatabaseContract.Tasks.CREATED_AT + " INTEGER NOT NULL, "
                + DatabaseContract.Tasks.UPDATED_AT + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + DatabaseContract.Tasks.CATEGORY_ID + ") "
                + "REFERENCES " + DatabaseContract.Categories.TABLE + "(" + DatabaseContract.Categories.ID + ") "
                + "ON DELETE SET NULL, "
                + "CHECK (length(trim(" + DatabaseContract.Tasks.TITLE + ")) > 0), "
                + "CHECK (" + DatabaseContract.Tasks.PRIORITY + " BETWEEN 0 AND 3), "
                + "CHECK (" + DatabaseContract.Tasks.REMINDER_ENABLED + " IN (0, 1)), "
                + "CHECK (" + DatabaseContract.Tasks.IS_COMPLETED + " IN (0, 1))"
                + ")");

        db.execSQL("CREATE INDEX idx_tasks_completed_due ON tasks(is_completed, due_at)");
        db.execSQL("CREATE INDEX idx_tasks_reminder ON tasks(reminder_enabled, reminder_at)");
        db.execSQL("CREATE INDEX idx_tasks_category ON tasks(category_id)");
        db.execSQL("CREATE INDEX idx_tasks_created ON tasks(created_at)");
        seedCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future schema changes should use incremental migrations.
    }

    private void seedCategories(SQLiteDatabase db) {
        insertCategory(db, "Pribadi", 0xFF2563EB);
        insertCategory(db, "Kuliah", 0xFF16A34A);
        insertCategory(db, "Pekerjaan", 0xFFD97706);
    }

    private void insertCategory(SQLiteDatabase db, String name, int color) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Categories.NAME, name);
        values.put(DatabaseContract.Categories.COLOR, color);
        values.put(DatabaseContract.Categories.CREATED_AT, now);
        values.put(DatabaseContract.Categories.UPDATED_AT, now);
        db.insert(DatabaseContract.Categories.TABLE, null, values);
    }
}
