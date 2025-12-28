package com.example.smart_study_planner_android.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study_planner_android.R;
import com.example.smart_study_planner_android.activities.PomodoroActivity;
import com.example.smart_study_planner_android.activities.database.TaskDAO;
import com.example.smart_study_planner_android.activities.model.Task;

import java.util.List;

public class TaskRecyclerAdapter
        extends RecyclerView.Adapter<TaskRecyclerAdapter.ViewHolder> {

    private List<Task> tasks;
    private final TaskDAO dao;
    private final int status;

    private final TaskActionListener listener;


    public TaskRecyclerAdapter(Context ctx,
                               List<Task> tasks,
                               int status,
                               TaskActionListener listener) {
        this.tasks = tasks;
        this.dao = new TaskDAO(ctx);
        this.status = status;
        this.listener = listener;
    }


    public void refresh(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout;

        switch (status) {
            case TaskDAO.TODO:
                layout = R.layout.item_task_todo;
                break;

            case TaskDAO.RUNNING:
                layout = R.layout.item_task_running;
                break;

            case TaskDAO.PAUSED:
                layout = R.layout.item_task_paused;
                break;

            case TaskDAO.COMPLETED:
            default:
                layout = R.layout.item_task_completed;
                break;
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Task t = tasks.get(pos);
        h.title.setText(t.getTitle());

        // Start button
        if (h.btnStart != null) {
            h.btnStart.setOnClickListener(v -> {

                // Update status
                dao.updateStatus(t.getId(), TaskDAO.RUNNING);

                // Open Pomodoro
                Intent i = new Intent(v.getContext(), PomodoroActivity.class);
                i.putExtra("task_id", t.getId());
                i.putExtra("task_title", t.getTitle());
                v.getContext().startActivity(i);

                //  Remove from current list
                listener.onTaskChanged();

            });
        }

        // Pause button
        if (h.btnPause != null) {
            h.btnPause.setOnClickListener(v -> {
                dao.updateStatus(t.getId(), TaskDAO.PAUSED);
                listener.onTaskChanged();

            });
        }

        // Finish button
        if (h.btnFinish != null) {
            h.btnFinish.setOnClickListener(v -> {
                dao.updateStatus(t.getId(), TaskDAO.COMPLETED);
                listener.onTaskChanged();

            });
        }

        // Delete button
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> {
                dao.deleteTask(t.getId());
                listener.onTaskChanged();
            });
        }

        // Item click for RUNNING tasks to open Pomodoro
        if (status == TaskDAO.RUNNING) {
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), PomodoroActivity.class);
                i.putExtra("task_id", t.getId());
                i.putExtra("task_title", t.getTitle());
                v.getContext().startActivity(i);
            });
        } else {
            h.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        Button btnStart, btnPause, btnFinish, btnDelete;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            btnStart = v.findViewById(R.id.btnStart);
            btnPause = v.findViewById(R.id.btnPause);
            btnFinish = v.findViewById(R.id.btnFinish);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
