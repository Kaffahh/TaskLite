package com.kaffah.tasklite.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaffah.tasklite.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private final DatabaseHelper databaseHelper;

    public CategoryRepository(Context context) {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public List<Category> getAllCategories() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<Category> categories = new ArrayList<>();
        try (Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.Categories.NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                categories.add(readCategory(cursor));
            }
        }
        return categories;
    }

    public long insert(String name, int color) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Categories.NAME, name.trim());
        values.put(DatabaseContract.Categories.COLOR, color);
        values.put(DatabaseContract.Categories.CREATED_AT, now);
        values.put(DatabaseContract.Categories.UPDATED_AT, now);
        return databaseHelper.getWritableDatabase().insert(DatabaseContract.Categories.TABLE, null, values);
    }

    public int update(Category category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Categories.NAME, category.name.trim());
        values.put(DatabaseContract.Categories.COLOR, category.color);
        values.put(DatabaseContract.Categories.UPDATED_AT, System.currentTimeMillis());
        return databaseHelper.getWritableDatabase().update(
                DatabaseContract.Categories.TABLE,
                values,
                DatabaseContract.Categories.ID + " = ?",
                new String[]{String.valueOf(category.id)});
    }

    public int delete(long categoryId) {
        return databaseHelper.getWritableDatabase().delete(
                DatabaseContract.Categories.TABLE,
                DatabaseContract.Categories.ID + " = ?",
                new String[]{String.valueOf(categoryId)});
    }

    public boolean isNameUsed(String name, long exceptId) {
        String sql = "SELECT COUNT(*) FROM " + DatabaseContract.Categories.TABLE
                + " WHERE " + DatabaseContract.Categories.NAME + " = ? COLLATE NOCASE"
                + " AND " + DatabaseContract.Categories.ID + " != ?";
        try (Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                sql,
                new String[]{name.trim(), String.valueOf(exceptId)})) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    private Category readCategory(Cursor cursor) {
        return new Category(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.NAME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLOR)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.CREATED_AT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.UPDATED_AT))
        );
    }
}
