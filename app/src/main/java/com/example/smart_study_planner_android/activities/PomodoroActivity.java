package com.example.smart_study_planner_android.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;

public class PomodoroActivity extends AppCompatActivity {

    private static final long STUDY_TIME = 25 * 60 * 1000; // 25 min

    private CountDownTimer timer;
    private boolean isRunning = true;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        TextView tvTask = findViewById(R.id.tvTask);
        TextView tvTimer = findViewById(R.id.tvTimer);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnFinish = findViewById(R.id.btnFinish);

        taskId = getIntent().getIntExtra("task_id", -1);
        String title = getIntent().getStringExtra("task_title");

        tvTask.setText(title);

        startTimer(STUDY_TIME, tvTimer);

        btnPause.setOnClickListener(v -> {
            if (isRunning) {
                timer.cancel();
                btnPause.setText("Resume");
            } else {
                startTimer(parseTime(tvTimer.getText().toString()), tvTimer);
                btnPause.setText("Pause");
            }
            isRunning = !isRunning;
        });

        btnFinish.setOnClickListener(v -> {
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
                tv.setText("Done!");
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
