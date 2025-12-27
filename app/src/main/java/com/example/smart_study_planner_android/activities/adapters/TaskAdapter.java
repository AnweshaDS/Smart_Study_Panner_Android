package com.example.smart_study_planner_android.activities.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {

    private final TaskDAO dao;
    private final int status;

    public TaskAdapter(Context ctx, List<Task> tasks) {
        super(ctx, 0, tasks);
        this.dao = new TaskDAO(ctx);
        this.status = status;
    }

    @Override
    public View getView(int pos, View v, ViewGroup parent) {

        Task t = getItem(pos);

        if (v == null) {
            int layout = switch (status) {
                case TaskDAO.TODO -> R.layout.item_task_todo;
                case TaskDAO.RUNNING -> R.layout.item_task_running;
                case TaskDAO.PAUSED -> R.layout.item_task_paused;
                default -> R.layout.item_task_completed;
            };
            v = LayoutInflater.from(getContext()).inflate(layout, parent, false);
        }

        TextView title = v.findViewById(R.id.tvTitle);
        title.setText(t.getTitle());

        setupButtons(v, t);

        return v;
    }

    private void setupButtons(View v, Task t) {

        if (status == TaskDAO.TODO) {
            v.findViewById(R.id.btnStart).setOnClickListener(
                    b -> dao.updateStatus(t.getId(), TaskDAO.RUNNING)
            );
        }

        if (status == TaskDAO.RUNNING) {
            v.findViewById(R.id.btnPause).setOnClickListener(
                    b -> dao.updateStatus(t.getId(), TaskDAO.PAUSED)
            );
            v.findViewById(R.id.btnFinish).setOnClickListener(
                    b -> dao.updateStatus(t.getId(), TaskDAO.COMPLETED)
            );
        }

        if (status == TaskDAO.PAUSED) {
            v.findViewById(R.id.btnRun).setOnClickListener(
                    b -> dao.updateStatus(t.getId(), TaskDAO.RUNNING)
            );
            v.findViewById(R.id.btnFinish).setOnClickListener(
                    b -> dao.updateStatus(t.getId(), TaskDAO.COMPLETED)
            );
        }

        if (v.findViewById(R.id.btnDelete) != null) {
            v.findViewById(R.id.btnDelete).setOnClickListener(
                    b -> dao.deleteTask(t.getId())
            );
        }
    }
}
