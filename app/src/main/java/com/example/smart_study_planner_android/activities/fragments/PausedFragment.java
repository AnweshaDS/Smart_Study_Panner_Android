package com.example.smart_study_planner_android.activities.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.adapters.TaskAdapter;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class PausedFragment extends Fragment {

    private TaskDAO dao;
    private List<Task> tasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_paused, container, false);
        dao = new TaskDAO(requireContext());

        ListView list = v.findViewById(R.id.listTasks);
        load(list);

        list.setOnItemLongClickListener((p, view, pos, id) -> {
            Task t = tasks.get(pos);

            String[] options = {"Resume", "Finish"};
            new AlertDialog.Builder(requireContext())
                    .setTitle(t.getTitle())
                    .setItems(options, (d, which) -> {
                        if (which == 0)
                            dao.updateStatus(t.getId(), TaskDAO.RUNNING);
                        else
                            dao.updateStatus(t.getId(), TaskDAO.COMPLETED);
                        load(list);
                    }).show();
            return true;
        });

        return v;
    }

    private void load(ListView list) {
        tasks = dao.getTasksByStatus(TaskDAO.PAUSED);
        list.setAdapter(new TaskAdapter(requireContext(), tasks));
    }
}
