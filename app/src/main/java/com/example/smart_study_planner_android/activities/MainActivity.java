package com.example.smart_study_planner_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.fragments.CompletedFragment;
import com.example.smart_study_planner_android.activities.fragments.PausedFragment;
import com.example.smart_study_planner_android.activities.fragments.RunningFragment;
import com.example.smart_study_planner_android.activities.fragments.TodoFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // DB init
        TaskDAO dao = new TaskDAO(this);
        dao.createTable();

        // Auth listener (single source of truth)
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        };

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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
    }

    // ===== MENU =====

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
