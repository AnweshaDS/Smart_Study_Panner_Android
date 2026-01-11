package com.example.smart_study_planner_android.activities.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.smart_study_planner_android.activities.DateUtil;
import com.example.smart_study_planner_android.activities.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class TaskDAO {

    private final UserDAO helper;
    private final Context context;
    private FirebaseFirestore firestore;
    private String uid;
    public static final int TODO = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED = 2;
    public static final int COMPLETED = 3;

    public TaskDAO(Context context) {
        this.context = context;
        helper = new UserDAO(context);
        firestore = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

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

        long id= db.insert("tasks", null, cv);
        task.setId((int) id);
        db.close();
        syncTaskToFirestore(task);

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
        String today = DateUtil.today();
        Cursor c = db.rawQuery(
                "SELECT SUM(today_spent_seconds) FROM tasks WHERE last_study_date=?",
                new String[]{today}
        );
        long total = 0;
        if (c.moveToFirst()) {
            total = c.getLong(0);
        }
        c.close();
        db.close();
        return total;
    }

    private void syncTaskToFirestore(Task task) {
        if (uid == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("title", task.getTitle());
        map.put("status", task.getStatus());
        map.put("targetSeconds", task.getTargetSeconds());
        map.put("studySeconds", task.getStudySeconds());
        map.put("breakSeconds", task.getBreakSeconds());
        map.put("spentSeconds", task.getSpentSeconds());
        map.put("todaySpentSeconds", task.getTodaySpentSeconds());
        map.put("lastStudyDate", task.getLastStudyDate());

        firestore.collection("users")
                .document(uid)
                .collection("tasks")
                .document(String.valueOf(task.getId()))
                .set(map);
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
        Task task = getTaskById(id);
        if (task != null) {
            syncTaskToFirestore(task);
        }

        db.close();
    }



    public void updateStatus(int taskId, int newStatus) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});
        Task task = getTaskById(taskId);
        if (task != null) {
            syncTaskToFirestore(task);
        }
        db.close();
    }

    public void updateTodaySpentTime(int taskId, long seconds, String date) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("today_spent_seconds", seconds);
        cv.put("last_study_date", date);

        db.update("tasks", cv, "id=?", new String[]{String.valueOf(taskId)});
        Task task = getTaskById(taskId);
        if (task != null) {
            syncTaskToFirestore(task);
        }

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

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = sdf.format(new Date());


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
        if (lastStart <= 0) {
            c.close();
            db.close();
            return;
        }

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
        Task task = getTaskById(taskId);
        if (task != null) syncTaskToFirestore(task);

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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .collection("tasks")
                .document(String.valueOf(id))
                .delete();
    }
    public void clearAllTasks() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("tasks", null, null);
        db.close();
    }


    public void syncFromFirestore() {
        if (uid == null) return;

        firestore.collection("users")
                .document(uid)
                .collection("tasks")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot.getDocuments()) {

                        Task task = new Task(
                                Integer.parseInt(doc.getId()),
                                doc.getString("title"),
                                doc.getLong("status").intValue(),
                                doc.getLong("targetSeconds"),
                                doc.getLong("studySeconds"),
                                doc.getLong("breakSeconds"),
                                doc.getLong("spentSeconds"),
                                0,
                                doc.getLong("todaySpentSeconds"),
                                doc.getString("lastStudyDate")
                        );

                        upsertTask(task);
                    }
                });


    }

    public void upsertTask(Task task) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("id", task.getId());
        cv.put("title", task.getTitle());
        cv.put("status", task.getStatus());
        cv.put("targetSeconds", task.getTargetSeconds());
        cv.put("studySeconds", task.getStudySeconds());
        cv.put("breakSeconds", task.getBreakSeconds());
        cv.put("spentSeconds", task.getSpentSeconds());
        cv.put("lastStartTime", task.getLastStartTime());
        cv.put("today_spent_seconds", task.getTodaySpentSeconds());
        cv.put("last_study_date", task.getLastStudyDate());

        int rows = db.update(
                "tasks",
                cv,
                "id=?",
                new String[]{String.valueOf(task.getId())}
        );

        if (rows == 0) {
            db.insert("tasks", null, cv);
        }

        db.close();
    }



}
