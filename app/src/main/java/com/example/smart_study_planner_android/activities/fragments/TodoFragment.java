package com.example.smart_study_planner_android.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.adapters.TaskRecyclerAdapter;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class TodoFragment extends Fragment {

    private TaskDAO dao;
    private TaskRecyclerAdapter adapter;
    private List<Task> tasks;

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

        View v = inflater.inflate(R.layout.fragment_todo, container, false);

        dao = new TaskDAO(requireContext());

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
                        0
                );

                dao.addTask(task);

                etTask.setText("");
                refreshTasks();
            }
        });

        return v;
    }

    private void refreshTasks() {
        tasks = dao.getTasksByStatus(TaskDAO.TODO);
        adapter.refresh(tasks);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTasks();
    }


}
