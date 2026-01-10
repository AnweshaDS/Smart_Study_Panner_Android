package com.example.smart_study_planner_android.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.LoginActivity;
import com.example.smart_study_planner_android.activities.adapters.TaskRecyclerAdapter;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;



public class TodoFragment extends Fragment {

    private TaskDAO dao;
    private TaskRecyclerAdapter adapter;
    private List<Task> tasks;
    private TextView tvDailyTotal;


    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());
    }


    private long parseHMS(String time) {
        String[] p = time.split(":");
        if (p.length != 3) return 0;
        return Integer.parseInt(p[0]) * 3600L
                + Integer.parseInt(p[1]) * 60L
                + Integer.parseInt(p[2]);
    }


    @Nullable
    @Override

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        ensureLoggedIn();
        View v = inflater.inflate(R.layout.fragment_todo, container, false);

        dao = new TaskDAO(requireContext());

        tvDailyTotal = v.findViewById(R.id.tvDailyTotal);
        EditText etTask = v.findViewById(R.id.etTask);
        EditText etTarget = v.findViewById(R.id.etTargetMinutes);
        EditText etStudy = v.findViewById(R.id.etStudyMinutes);
        EditText etBreak = v.findViewById(R.id.etBreakMinutes);
        Button btnAdd = v.findViewById(R.id.btnAdd);
        RecyclerView recycler = v.findViewById(R.id.recyclerTasks);
        if (recycler == null) {
            throw new RuntimeException("RecyclerView not found in fragment layout");
        }


        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasks = dao.getTasksByStatus(TaskDAO.TODO);
        adapter = new TaskRecyclerAdapter(requireContext(),
                tasks,
                TaskDAO.TODO,
                this::refreshTasks);
        recycler.setAdapter(adapter);

        btnAdd.setOnClickListener(view -> {
            String title = etTask.getText().toString().trim();
            if (!title.isEmpty()) {
                long targetSec = parseHMS(etTarget.getText().toString());
                long studySec  = parseHMS(etStudy.getText().toString());
                long breakSec  = parseHMS(etBreak.getText().toString());

                Task task = new Task(
                        0,
                        title,
                        TaskDAO.TODO,
                        targetSec,
                        studySec,
                        breakSec,
                        0,
                        0,
                        0,
                        today()
                );

                dao.addTask(task);

                etTask.setText("");
                refreshTasks();
            }
        });

        return v;
    }
    private void ensureLoggedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
        }
    }



    private String format(long sec) {
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }


    private void refreshTasks() {
        tasks = dao.getTasksByStatus(TaskDAO.TODO);
        adapter.refresh(tasks);

        long total = dao.getTodayTotalSeconds();
        tvDailyTotal.setText("Today: " + format(total));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTasks();
    }


}
