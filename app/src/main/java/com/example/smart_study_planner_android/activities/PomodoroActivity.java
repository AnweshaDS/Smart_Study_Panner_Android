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
    TaskDAO dao;
    Task task;

    long studyTimeMs;


    private CountDownTimer timer;
    private boolean isRunning = true;
    private int taskId;
    private long sessionStartTime;
    private boolean isStudyPhase = true;


    private void saveSpentTime() {
        long now = System.currentTimeMillis();
        long deltaSeconds = (now - sessionStartTime) / 1000;

        task.setSpentSeconds(task.getSpentSeconds() + deltaSeconds);
        dao.updateSpentTime(task.getId(), task.getSpentSeconds());

        sessionStartTime = now; // reset
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
        sessionStartTime = System.currentTimeMillis();



        String title = getIntent().getStringExtra("task_title");

        tvTask.setText(title);

        startTimer(studyTimeMs, tvTimer);
        task.setLastStartTime(System.currentTimeMillis());



        btnPause.setOnClickListener(v -> {
            if (isRunning) {
                timer.cancel();
                saveSpentTime();
                btnPause.setText("Resume");
            } else {
                sessionStartTime = System.currentTimeMillis();
                startTimer(parseTime(tvTimer.getText().toString()), tvTimer);
                btnPause.setText("Pause");
            }
            isRunning = !isRunning;
        });

        btnFinish.setOnClickListener(v -> {
            saveSpentTime();
            new TaskDAO(this).updateStatus(taskId, TaskDAO.COMPLETED);
            finish();
        });
    }

    private void startTimer(long millis, TextView tv) {
        timer = new CountDownTimer(millis, 1000) {

            public void onTick(long ms) {
                tv.setText(format(ms));
            }

            public void onFinish() {
                saveSpentTime();

                if (isStudyPhase) {
                    // switch to BREAK
                    isStudyPhase = false;
                    startTimer(task.getBreakSeconds() * 1000, tv);
                    tv.setText("Break");
                } else {
                    // session fully done
                    dao.updateStatus(taskId, TaskDAO.COMPLETED);
                    finish();
                }
            }

        }.start();
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
