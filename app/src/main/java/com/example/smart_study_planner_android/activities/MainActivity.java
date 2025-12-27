package com.example.smart_study_planner_android.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.fragments.CompletedFragment;
import com.example.smart_study_planner_android.activities.fragments.PausedFragment;
import com.example.smart_study_planner_android.activities.fragments.RunningFragment;
import com.example.smart_study_planner_android.activities.fragments.TodoFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TaskDAO dao = new TaskDAO(this);
        dao.createTable();

        Button btnTodo = findViewById(R.id.btnTodo);
        Button btnRunning = findViewById(R.id.btnRunning);
        Button btnPaused = findViewById(R.id.btnPaused);
        Button btnCompleted = findViewById(R.id.btnCompleted);

        loadFragment(new TodoFragment());

        btnTodo.setOnClickListener(v -> loadFragment(new TodoFragment()));
        btnRunning.setOnClickListener(v -> loadFragment(new RunningFragment()));
        btnPaused.setOnClickListener(v -> loadFragment(new PausedFragment()));
        btnCompleted.setOnClickListener(v -> loadFragment(new CompletedFragment()));
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
