package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.techtoids.nota.databinding.TaskListItemBinding;
import com.techtoids.nota.model.BaseTask;
import com.techtoids.nota.view.viewholder.TaskViewHolder;

public class TaskListAdapter extends FirestoreRecyclerAdapter<BaseTask, TaskViewHolder> {
    private final OnItemClickListener onItemClickListener;
    private final View emptyView;
    private ItemTouchHelper itemTouchHelper;

    public TaskListAdapter(@NonNull FirestoreRecyclerOptions<BaseTask> options, OnItemClickListener onItemClickListener, View emptyView) {
        super(options);
        this.onItemClickListener = onItemClickListener;
        this.emptyView = emptyView;

        onDataChanged();
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull BaseTask model) {
        TaskViewHolder.bindViewHolder(holder, model);

        holder.binding.taskDrag.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder);
            }
            return false;
        });
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(TaskListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), onItemClickListener, parent.getContext());
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        System.out.println("changed");
        if (emptyView != null) {
            if (getItemCount() > 0) {
                emptyView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

}
