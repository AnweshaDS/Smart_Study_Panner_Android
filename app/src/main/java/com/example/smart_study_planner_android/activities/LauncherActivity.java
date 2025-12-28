package com.example.smart_study_planner_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.LoginActivity;

public class LauncherActivity extends AppCompatActivity {

    private ImageView logoView;
    private Button btnLogin, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        logoView = findViewById(R.id.logoView);
        btnLogin = findViewById(R.id.btnLogin);
        btnExit = findViewById(R.id.btnExit);


        logoView.setImageResource(R.drawable.logo);

        btnLogin.setOnClickListener(v -> {

            startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
            finish();
        });

        btnExit.setOnClickListener(v -> {

            finishAffinity();
        });
    }
}
