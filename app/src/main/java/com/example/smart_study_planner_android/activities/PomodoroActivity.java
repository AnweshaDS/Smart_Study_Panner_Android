package com.example.smart_study_planner_android.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;
import com.example.smart_study_planner_android.activities.DateUtil;

public class PomodoroActivity extends AppCompatActivity {

    private TaskDAO dao;
    private Task task;

    private long studyTimeMs;
    private long breakTimeMs;

    private CountDownTimer timer;
    private boolean isStudyPhase = true;

    private int taskId;
    private long sessionStartTime;
    private long phaseEndTime;

    private boolean targetWarningShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        TextView tvTask = findViewById(R.id.tvTask);
        TextView tvTimer = findViewById(R.id.tvTimer);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnFinish = findViewById(R.id.btnFinish);

        dao = new TaskDAO(this);
        taskId = getIntent().getIntExtra("task_id", -1);
        task = dao.getTaskById(taskId);

        if (task == null) {
            finish();
            return;
        }

        tvTask.setText(task.getTitle());

        studyTimeMs = task.getStudySeconds() * 1000;
        breakTimeMs = task.getBreakSeconds() * 1000;

        sessionStartTime = System.currentTimeMillis();
        startTimer(studyTimeMs, tvTimer);

        btnPause.setOnClickListener(v -> {
            timer.cancel();
            saveAllTime();
            dao.updateStatus(taskId, TaskDAO.PAUSED);
            finish();
        });

        btnFinish.setOnClickListener(v -> {
            timer.cancel();
            saveAllTime();
            dao.updateStatus(taskId, TaskDAO.COMPLETED);
            finish();
        });
    }

    private void startTimer(long millis, TextView tv) {

        phaseEndTime = System.currentTimeMillis() + millis;

        timer = new CountDownTimer(millis, 1000) {

            @Override
            public void onTick(long ignored) {
                long remaining = phaseEndTime - System.currentTimeMillis();
                if (remaining < 0) remaining = 0;
                tv.setText(format(remaining));
            }

            @Override
            public void onFinish() {
                tv.setText("00:00:00");
                saveAllTime();

                sessionStartTime = System.currentTimeMillis();

                if (isStudyPhase) {
                    isStudyPhase = false;
                    startTimer(breakTimeMs, tv);
                } else {
                    isStudyPhase = true;
                    startTimer(studyTimeMs, tv);
                }
            }
        }.start();
    }

    private void saveAllTime() {
        long now = System.currentTimeMillis();
        long deltaSeconds = (now - sessionStartTime) / 1000;
        if (deltaSeconds <= 0) return;

        long newSpent = task.getSpentSeconds() + deltaSeconds;
        task.setSpentSeconds(newSpent);

        String today = DateUtil.today();
        if (!today.equals(task.getLastStudyDate())) {
            task.setTodaySpentSeconds(0);
        }

        task.setTodaySpentSeconds(task.getTodaySpentSeconds() + deltaSeconds);
        task.setLastStudyDate(today);

        dao.upsertTask(task);
        sessionStartTime = now;

        checkTargetExceeded();
    }

    private void checkTargetExceeded() {
        if (targetWarningShown) return;

        if (task.getTargetSeconds() > 0 &&
                task.getSpentSeconds() >= task.getTargetSeconds()) {

            targetWarningShown = true;

            new AlertDialog.Builder(this)
                    .setTitle("Target Time Reached")
                    .setMessage(
                            "You have reached your planned study time.\n\n" +
                                    "You may continue or finish the task."
                    )
                    .setPositiveButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private String format(long ms) {
        long totalSec = ms / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
