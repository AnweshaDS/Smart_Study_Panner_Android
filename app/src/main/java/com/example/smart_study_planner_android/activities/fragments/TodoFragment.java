package com.example.smart_study_planner_android.activities.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.adapters.TaskAdapter;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class TodoFragment extends Fragment {

    private TaskDAO dao;
    private ArrayAdapter<String> adapter;
    private List<Task> tasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_todo, container, false);

        dao = new TaskDAO(requireContext());

        EditText etTask = v.findViewById(R.id.etTask);
        Button btnAdd = v.findViewById(R.id.btnAdd);
        ListView list = v.findViewById(R.id.listTasks);

        loadTasks(list);

        btnAdd.setOnClickListener(view -> {
            String title = etTask.getText().toString().trim();
            if (!title.isEmpty()) {
                dao.addTask(new Task(title, TaskDAO.TODO));
                etTask.setText("");
                loadTasks(list);
            }
        });

        // Long press → Start task
        list.setOnItemLongClickListener((parent, view, pos, id) -> {
            Task t = tasks.get(pos);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Start Task")
                    .setMessage("Start this task?")
                    .setPositiveButton("Start", (d, w) -> {
                        dao.updateStatus(t.getId(), TaskDAO.RUNNING);
                        loadTasks(list);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        return v;
    }

    private void loadTasks(ListView list) {
        tasks = dao.getTasksByStatus(TaskDAO.TODO);
        list.setAdapter(new TaskAdapter(requireContext(), tasks));

        list.setAdapter(adapter);
    }
}
