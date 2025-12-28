package com.example.smart_study_planner_android.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class RunningFragment extends Fragment {

    private TaskDAO dao;
    private TaskRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View v = inflater.inflate(R.layout.fragment_running, container, false);

        dao = new TaskDAO(requireContext());

        RecyclerView recycler = v.findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TaskRecyclerAdapter(
                requireContext(),
                dao.getTasksByStatus(TaskDAO.RUNNING),
                TaskDAO.RUNNING,
                this::refreshTasks
        );

        recycler.setAdapter(adapter);
        return v;
    }

    private void refreshTasks() {
        adapter.refresh(dao.getTasksByStatus(TaskDAO.RUNNING));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTasks();
    }
}
