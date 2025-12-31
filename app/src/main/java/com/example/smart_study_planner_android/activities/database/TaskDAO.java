package com.example.smart_study_planner_android.activities.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.smart_study_planner_android.activities.DateUtil;
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
                        "lastStartTime INTEGER,"+
                        "today_spent_seconds INTEGER," +
                        "last_study_date TEXT)"

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
        cv.put("today_spent_seconds",task.getTodaySpentSeconds());
        cv.put("last_study_date",task.getLastStudyDate());

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
                    c.getLong(7),     // lastStartTime
                    c.getLong(8),
                    c.getString(9)
            ));
        }


        c.close();
        db.close();
        return list;
    }

    public long getTodayTotalSeconds() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(today_spent_seconds) FROM tasks",
                null
        );
        long total = 0;
        if (c.moveToFirst()) {
            total = c.getLong(0);
        }
        c.close();
        return total;
    }


    public Task getTaskById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM tasks WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        Task t = null;
        if (c.moveToFirst()) {
            t = new Task(
                    c.getInt(0),
                    c.getString(1),
                    c.getInt(2),
                    c.getLong(3),
                    c.getLong(4),
                    c.getLong(5),
                    c.getLong(6),
                    c.getLong(7),
                    c.getLong(8),
                    c.getString(9)
            );
        }

        c.close();
        db.close();
        return t;
    }

    public void updateSpentTime(int id, long spent) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("spentSeconds", spent);
        db.update("tasks", cv, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }



    public void updateStatus(int taskId, int newStatus) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void updateTodaySpentTime(int taskId, long seconds, String date) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("today_spent_seconds", seconds);
        cv.put("last_study_date", date);

        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void addStudySession(int taskId, long sessionSeconds) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // get current values
        Cursor c = db.rawQuery(
                "SELECT spentSeconds, today_spent_seconds, last_study_date FROM tasks WHERE id=?",
                new String[]{String.valueOf(taskId)}
        );

        if (!c.moveToFirst()) {
            c.close();
            db.close();
            return;
        }

        long totalSpent = c.getLong(0);
        long todaySpent = c.getLong(1);
        String lastDate = c.getString(2);

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        String today = sdf.format(new java.util.Date());


        // reset daily if date changed
        if (lastDate == null || !lastDate.equals(today)) {
            todaySpent = 0;
        }

        totalSpent += sessionSeconds;
        todaySpent += sessionSeconds;

        ContentValues cv = new ContentValues();
        cv.put("spentSeconds", totalSpent);
        cv.put("today_spent_seconds", todaySpent);
        cv.put("last_study_date", today);

        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});

        c.close();
        db.close();
    }

    public void saveSessionTime(int taskId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT spentSeconds, lastStartTime, today_spent_seconds, last_study_date " +
                        "FROM tasks WHERE id=?",
                new String[]{String.valueOf(taskId)}
        );

        if (!c.moveToFirst()) {
            c.close();
            db.close();
            return;
        }

        long spent = c.getLong(0);
        long lastStart = c.getLong(1);
        long todaySpent = c.getLong(2);
        String lastDate = c.getString(3);

        long now = System.currentTimeMillis() / 1000;
        long delta = now - lastStart;
        if (delta < 0) delta = 0;

        String today = DateUtil.today();

        if (lastDate == null || !lastDate.equals(today)) {
            todaySpent = 0; // reset daily total if new day
        }

        ContentValues cv = new ContentValues();
        cv.put("spentSeconds", spent + delta);
        cv.put("today_spent_seconds", todaySpent + delta);
        cv.put("last_study_date", today);
        cv.put("lastStartTime", 0);

        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});

        c.close();
        db.close();
    }

    public void markTaskStarted(int taskId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("lastStartTime", System.currentTimeMillis() / 1000);

        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});
        db.close();
    }




    public void deleteTask(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("tasks", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

}
