package com.example.smart_study_planner_android.activities.ui;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeCallback extends ItemTouchHelper.SimpleCallback {

    private final SwipeListener listener;

    public interface SwipeListener {
        void onSwipeLeft(int pos);
        void onSwipeRight(int pos);
    }

    public SwipeCallback(SwipeListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView r, RecyclerView.ViewHolder a,
                          RecyclerView.ViewHolder b) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
        if (dir == ItemTouchHelper.LEFT)
            listener.onSwipeLeft(vh.getAdapterPosition());
        else
            listener.onSwipeRight(vh.getAdapterPosition());
    }
}
