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

    long studyTime;
    long breakTime;


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

        sessionStartTime = now; // reset for next session
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

        studyTime = task.getStudySeconds() * 1000; // seconds → millis
        breakTime = task.getBreakSeconds() * 1000;
        sessionStartTime = System.currentTimeMillis();



        String title = getIntent().getStringExtra("task_title");

        tvTask.setText(title);

        startTimer(studyTime, tvTimer);


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
                if (isStudyPhase) {
                    isStudyPhase = false;
                    startTimer(breakTime, tv);
                    tv.setText("Break!");
                } else {
                    tv.setText("Session Complete!");
                    dao.updateStatus(taskId, TaskDAO.COMPLETED);
                }
            }

        }.start();
    }

    private String format(long ms) {
        int m = (int) (ms / 60000);
        int s = (int) ((ms % 60000) / 1000);
        return String.format("%02d:%02d", m, s);
    }

    private long parseTime(String t) {
        String[] p = t.split(":");
        return (Integer.parseInt(p[0]) * 60L +
                Integer.parseInt(p[1])) * 1000;
    }
}
