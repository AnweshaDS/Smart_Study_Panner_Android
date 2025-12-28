package com.example.smart_study_planner_android.activities.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.adapters.TaskRecyclerAdapter;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class CompletedFragment extends Fragment {

    private TaskDAO dao;
    private TaskRecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_completed, container, false);
        dao = new TaskDAO(requireContext());

        RecyclerView recycler = v.findViewById(R.id.recyclerTasks);
        if (recycler == null) {
            throw new RuntimeException("RecyclerView not found in fragment layout");
        }

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TaskRecyclerAdapter(
                requireContext(),
                dao.getTasksByStatus(TaskDAO.COMPLETED),
                TaskDAO.COMPLETED
        );

        recycler.setAdapter(adapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.refresh(dao.getTasksByStatus(TaskDAO.COMPLETED));
    }
}
