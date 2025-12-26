package com.example.smart_study_planner_android.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.UserDAO;
import com.example.smart_study_planner_android.activities.model.User;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText username = findViewById(R.id.etUsername);
        EditText password = findViewById(R.id.etPassword);
        EditText email = findViewById(R.id.etEmail);
        Button btnRegister = findViewById(R.id.btnRegister);

        UserDAO dao = new UserDAO(this);

        btnRegister.setOnClickListener(v -> {
            boolean success = dao.register(
                    new User(
                            username.getText().toString(),
                            password.getText().toString(),
                            email.getText().toString()
                    )
            );

            if (success) {
                Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
