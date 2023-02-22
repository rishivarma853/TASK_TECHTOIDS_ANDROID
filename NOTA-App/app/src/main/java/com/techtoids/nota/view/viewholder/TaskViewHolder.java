package com.techtoids.nota.view.viewholder;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.R;
import com.techtoids.nota.adapter.OnItemClickListener;
import com.techtoids.nota.databinding.TaskListItemBinding;
import com.techtoids.nota.helper.BasicHelper;
import com.techtoids.nota.model.BaseTask;
import com.techtoids.nota.model.TaskStatus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    public final TaskListItemBinding binding;
    private final OnItemClickListener onItemClickListener;

    private final Context context;

    public TaskViewHolder(TaskListItemBinding binding, OnItemClickListener onItemClickListener, Context context) {
        super(binding.getRoot());
        this.binding = binding;
        this.onItemClickListener = onItemClickListener;
        this.context = context;

        binding.getRoot().setOnClickListener(v -> {
            this.onItemClickListener.onItemClick(getBindingAdapterPosition());
        });
    }

    public static void bindViewHolder(TaskViewHolder holder, BaseTask model) {
        holder.binding.taskTitle.setText(model.getTitle());
        int childTaskCount = model.getChildTasks().size();
        holder.binding.taskDue.setText(BasicHelper.getDaysDue(model.getDueDate()));
        if (model.getTaskStatus() == TaskStatus.COMPLETED) {
            holder.binding.taskDue.setVisibility(View.GONE);
        } else {
            holder.binding.taskDue.setVisibility(View.VISIBLE);
        }
        holder.setDueDateColor(model.getDueDate());
        holder.setTaskStatus(model.getTaskStatus());
        if (childTaskCount > 0) {
            holder.binding.subtaskCount.setText(String.valueOf(childTaskCount));
            holder.binding.subtaskIcon.setVisibility(View.VISIBLE);
            holder.binding.subtaskCount.setVisibility(View.VISIBLE);

            List<BaseTask> inProgressTask = model.getChildTasks().stream().filter(task -> task.getTaskStatus() == TaskStatus.IN_PROGRESS).collect(Collectors.toList());
            List<BaseTask> completedTask = model.getChildTasks().stream().filter(task -> task.getTaskStatus() == TaskStatus.COMPLETED).collect(Collectors.toList());

            int completePercent = (completedTask.size() * 100) / model.getChildTasks().size();
            int inProgressPercent = Math.min(((inProgressTask.size() * 100) / model.getChildTasks().size()) + completePercent, 100);

            holder.binding.taskProgressPercent.setText(completePercent + "%");
            holder.binding.taskProgressBar.setProgress(completePercent);
            holder.binding.taskProgressBar.setSecondaryProgress(inProgressPercent);
        } else {
            holder.binding.taskProgressPercent.setText("0%");
            holder.binding.taskProgressBar.setProgress(0);
            holder.binding.taskProgressBar.setSecondaryProgress(0);
            if (model.getTaskStatus() == TaskStatus.IN_PROGRESS) {
                holder.binding.taskProgressBar.setSecondaryProgress(100);
            } else if (model.getTaskStatus() == TaskStatus.COMPLETED) {
                holder.binding.taskProgressBar.setProgress(100);
                holder.binding.taskProgressPercent.setText("100%");
            }
            holder.binding.subtaskIcon.setVisibility(View.GONE);
            holder.binding.subtaskCount.setVisibility(View.GONE);
        }

    }

    public void setDueDateColor(Date date) {
        long dateBeforeInMs = date.getTime();
        long dateAfterInMs = new Date().getTime();
        long timeDiff = dateBeforeInMs - dateAfterInMs;
        long millisecondsDiff = TimeUnit.MILLISECONDS.convert(timeDiff, TimeUnit.MILLISECONDS);
        if (millisecondsDiff < 0) {
            binding.taskDue.setBackgroundColor(ContextCompat.getColor(context, R.color.error));
        } else{
            binding.taskDue.setBackgroundColor(ContextCompat.getColor(context, R.color.secondary_color));
        }
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        binding.taskStatus.setText(taskStatus.value);
        switch (taskStatus) {
            case TODO:
                binding.taskStatus.setTextColor(ContextCompat.getColor(context, R.color.task_todo_foreground));
                binding.taskStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.task_todo_background));
                break;
            case IN_PROGRESS:
                binding.taskStatus.setTextColor(ContextCompat.getColor(context, R.color.task_inprogress_foreground));
                binding.taskStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.task_inprogress_background));
                break;
            case COMPLETED:
                binding.taskStatus.setTextColor(ContextCompat.getColor(context, R.color.task_completed_foreground));
                binding.taskStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.task_completed_background));
                break;
        }
    }
}
