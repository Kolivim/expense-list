package com.example.test3;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final AppDatabaseHelper dbHelper;

    UserRepository(AppDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    // Сохраняем имя пользователя
    void saveUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(AppDatabaseHelper.COL_NAME, name.trim());
            db.insert(AppDatabaseHelper.TABLE_USERS, null, values);
        }
    }


    // Получаем список всех пользователей
    List getAllUsers() {
        List users = new ArrayList();

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     AppDatabaseHelper.TABLE_USERS,
                     new String[]{AppDatabaseHelper.COL_NAME},
                     null, null, null, null, null)) {

            while (cursor.moveToNext()) {
                users.add(cursor.getString(0));
            }
        }

        return users;
    }


}