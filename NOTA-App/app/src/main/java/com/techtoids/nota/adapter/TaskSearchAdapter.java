package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.TaskListItemBinding;
import com.techtoids.nota.model.BaseTask;
import com.techtoids.nota.view.viewholder.TaskViewHolder;

import java.util.List;

public class TaskSearchAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private final List<BaseTask> taskList;
    private final OnItemClickListener onItemClickListener;

    public TaskSearchAdapter(List<BaseTask> taskList, OnItemClickListener onItemClickListener) {
        this.taskList = taskList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(TaskListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), onItemClickListener, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        BaseTask model = taskList.get(position);
        TaskViewHolder.bindViewHolder(holder, model);
        holder.binding.taskDrag.setVisibility(View.GONE);
    }

    public BaseTask getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
