package com.example.smart_study_planner_android.activities.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;

public class CompletedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_completed, container, false);

        ListView list = v.findViewById(R.id.listTasks);
        TaskDAO dao = new TaskDAO(requireContext());

        list.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                dao.getTasksByStatus(TaskDAO.COMPLETED)
                        .stream().map(t -> t.getTitle()).toArray()
        ));

        return v;
    }
}
