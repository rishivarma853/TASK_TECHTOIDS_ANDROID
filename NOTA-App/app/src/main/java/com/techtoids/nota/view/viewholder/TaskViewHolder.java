package com.techtoids.nota.view.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.TaskListItemBinding;

public class TaskViewHolder extends RecyclerView.ViewHolder {

    final TaskListItemBinding binding;

    public TaskViewHolder(TaskListItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
