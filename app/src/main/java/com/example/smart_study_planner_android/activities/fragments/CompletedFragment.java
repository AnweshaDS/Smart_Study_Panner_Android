package com.example.smart_study_planner_android.activities.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.adapters.TaskAdapter;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class CompletedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_completed, container, false);

        ListView list = v.findViewById(R.id.listTasks);
        TaskDAO dao = new TaskDAO(requireContext());

        List<Task> tasks = dao.getTasksByStatus(TaskDAO.COMPLETED);
        list.setAdapter(new TaskAdapter(requireContext(), tasks,TaskDAO.COMPLETED));

        return v;
    }
}
