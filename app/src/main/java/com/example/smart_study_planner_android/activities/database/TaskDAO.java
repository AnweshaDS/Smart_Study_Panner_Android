package com.example.smart_study_planner_android.activities.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.smart_study_planner_android.activities.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    private final UserDAO helper;

    public static final int TODO = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED = 2;
    public static final int COMPLETED = 3;

    public TaskDAO(Context context) {
        helper = new UserDAO(context);
    }

    public void createTable() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "status INTEGER)"
        );
        db.close();
    }

    public void addTask(Task task) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", task.getTitle());
        cv.put("status", task.getStatus());
        db.insert("tasks", null, cv);
        db.close();
    }

    public List<Task> getTasksByStatus(int status) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM tasks WHERE status=?",
                new String[]{String.valueOf(status)}
        );

        while (c.moveToNext()) {
            list.add(new Task(
                    c.getInt(0),
                    c.getString(1),
                    c.getInt(2)
            ));
        }

        c.close();
        db.close();
        return list;
    }

    public void updateStatus(int taskId, int newStatus) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});
        db.close();
    }
}
