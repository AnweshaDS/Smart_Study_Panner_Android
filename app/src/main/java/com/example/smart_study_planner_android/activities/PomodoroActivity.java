package com.example.smart_study_planner_android.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

public class PomodoroActivity extends AppCompatActivity {

    private TaskDAO dao;
    private Task task;
    private boolean targetWarningShown = false;

    private long studyTimeMs;
    private long breakTimeMs;

    private CountDownTimer timer;
    private boolean isRunning = true;
    private boolean isStudyPhase = true;

    private int taskId;
    private long sessionStartTime;

    // TIME SAVING

    private void saveAllTime() {
        long now = System.currentTimeMillis();
        long deltaSeconds = (now - sessionStartTime) / 1000;

        if (deltaSeconds <= 0) return;

        // total task time
        task.setSpentSeconds(task.getSpentSeconds() + deltaSeconds);
        dao.updateSpentTime(task.getId(), task.getSpentSeconds());

        // daily time
        String today = DateUtil.today();
        if (!today.equals(task.getLastStudyDate())) {
            task.setTodaySpentSeconds(0);
        }

        task.setTodaySpentSeconds(task.getTodaySpentSeconds() + deltaSeconds);
        dao.updateTodaySpentTime(
                task.getId(),
                task.getTodaySpentSeconds(),
                today
        );

        sessionStartTime = now;
        checkTargetExceeded();
    }

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

        studyTimeMs = task.getStudySeconds() * 1000;
        breakTimeMs = task.getBreakSeconds() * 1000;

        tvTask.setText(task.getTitle());

        sessionStartTime = System.currentTimeMillis();
        startTimer(studyTimeMs, tvTimer);

        btnPause.setOnClickListener(v -> {
            if (isRunning) {
                timer.cancel();
                saveAllTime();
                btnPause.setText("Resume");
            } else {
                sessionStartTime = System.currentTimeMillis();
                startTimer(parseTime(tvTimer.getText().toString()), tvTimer);
                btnPause.setText("Pause");
            }
            isRunning = !isRunning;
        });


        btnFinish.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            saveAllTime();
            dao.updateStatus(taskId, TaskDAO.COMPLETED);
            finish();
        });
    }

    //TIMER

    private void startTimer(long millis, TextView tv) {
        timer = new CountDownTimer(millis, 1000) {

            @Override
            public void onTick(long ms) {
                tv.setText(format(ms));
            }

            @Override
            public void onFinish() {
                saveAllTime();

                if (isStudyPhase) {
                    // switch → BREAK
                    isStudyPhase = false;
                    sessionStartTime = System.currentTimeMillis();
                    tv.setText("Break");
                    startTimer(breakTimeMs, tv);
                } else {
                    // switch → STUDY (loop continues)
                    isStudyPhase = true;
                    sessionStartTime = System.currentTimeMillis();
                    tv.setText("Study");
                    startTimer(studyTimeMs, tv);
                }
            }
        }.start();
    }
    private void checkTargetExceeded() {
        if (targetWarningShown) return;

        if (task.getTargetSeconds() > 0 &&
                task.getSpentSeconds() >= task.getTargetSeconds()) {

            targetWarningShown = true;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Target Time Reached")
                    .setMessage(
                            "You have reached your planned study time for this task.\n\n" +
                                    "You can continue studying or finish the task anytime."
                    )
                    .setPositiveButton("Continue", null)
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

    private long parseTime(String t) {
        String[] p = t.split(":");
        long sec = Integer.parseInt(p[0]) * 3600L
                + Integer.parseInt(p[1]) * 60L
                + Integer.parseInt(p[2]);
        return sec * 1000;
    }
}
