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
                        "status INTEGER," +
                        "targetSeconds INTEGER," +
                        "studySeconds INTEGER," +
                        "breakSeconds INTEGER," +
                        "spentSeconds INTEGER," +
                        "lastStartTime INTEGER)"

        );
        db.close();
    }

    public void addTask(Task task) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("title", task.getTitle());
        cv.put("status", task.getStatus());
        cv.put("targetSeconds", task.getTargetSeconds());
        cv.put("studySeconds", task.getStudySeconds());
        cv.put("breakSeconds", task.getBreakSeconds());
        cv.put("spentSeconds", task.getSpentSeconds());
        cv.put("lastStartTime", task.getLastStartTime());

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
                    c.getInt(0),      // id
                    c.getString(1),   // title
                    c.getInt(2),      // status
                    c.getLong(3),     // targetSeconds
                    c.getLong(4),     // studySeconds
                    c.getLong(5),     // breakSeconds
                    c.getLong(6),     // spentSeconds
                    c.getLong(7)      // lastStartTime
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
    public void deleteTask(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("tasks", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

}
