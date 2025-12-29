package com.example.smart_study_planner_android.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        if (h.title != null) {
            h.title.setText(t.getTitle());
        }

        if (h.timeInfo != null) {
            h.timeInfo.setText(
                    "Spent: " + t.getSpentTimeFormatted()
                            + " | Remaining: " + t.getRemainingTimeFormatted()
            );
        }



        // START
        if (h.btnStart != null) {
            h.btnStart.setOnClickListener(v -> {
                h.btnStart.setEnabled(false);
                dao.updateStatus(t.getId(), TaskDAO.RUNNING);

                Intent i = new Intent(v.getContext(), PomodoroActivity.class);
                i.putExtra("task_id", t.getId());
                i.putExtra("task_title", t.getTitle());
                v.getContext().startActivity(i);

                if (listener != null) {
                    v.post(listener::onTaskChanged);

                }

            });
        }

        // PAUSE
        if (h.btnPause != null) {
            h.btnPause.setOnClickListener(v -> {
                dao.updateStatus(t.getId(), TaskDAO.PAUSED);
                if (listener != null) {
                    v.post(listener::onTaskChanged);

                }

            });
        }

        // FINISH
        if (h.btnFinish != null) {
            h.btnFinish.setOnClickListener(v -> {
                dao.updateStatus(t.getId(), TaskDAO.COMPLETED);
                if (listener != null) {
                    v.post(listener::onTaskChanged);

                }

            });
        }

        // DELETE
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> {
                dao.deleteTask(t.getId());
                Toast.makeText(v.getContext(),
                        "Task deleted",
                        Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    v.post(listener::onTaskChanged);

                }

            });
        }

        // RUNNING → open Pomodoro
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView timeInfo;

        Button btnStart, btnPause, btnFinish, btnDelete;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            timeInfo = v.findViewById(R.id.tvTimeInfo);

            btnStart = v.findViewById(R.id.btnStart);
            btnPause = v.findViewById(R.id.btnPause);
            btnFinish = v.findViewById(R.id.btnFinish);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
