package com.example.smart_study_planner_android.activities.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smart_study_planner_android.activities.model.User;

public class UserDAO extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_planner.db";
    private static final int DB_VERSION = 1;

    public UserDAO(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT," +
                        "email TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public User login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        User user = null;
        if (c.moveToFirst()) {
            user = new User(
                    c.getInt(0),      // id
                    c.getString(1),   // username
                    c.getString(2),   // password
                    c.getString(3)    // email
            );

        }

        c.close();
        db.close();
        return user;
    }

    public boolean signup(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user.getUsername());
        cv.put("password", user.getPassword());
        cv.put("email", user.getEmail());

        long result = db.insert("users", null, cv);
        db.close();

        return result != -1;
    }

}
