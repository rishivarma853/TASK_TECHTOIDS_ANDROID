package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.TaskListItemBinding;
import com.techtoids.nota.helper.BasicHelper;
import com.techtoids.nota.model.BaseTask;
import com.techtoids.nota.view.viewholder.TaskViewHolder;

import java.util.List;

public class ChildTaskListAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private final OnItemClickListener onItemClickListener;
    private List<BaseTask> taskList;
    private ItemTouchHelper itemTouchHelper;

    public ChildTaskListAdapter(List<BaseTask> taskList, OnItemClickListener onItemClickListener) {
        this.taskList = taskList;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateList(List<BaseTask> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(TaskListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), onItemClickListener, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        BaseTask model = taskList.get(position);
        holder.binding.taskTitle.setText(model.getTitle());
        holder.setTaskStatus(model.getTaskStatus());
        holder.binding.taskDue.setText(BasicHelper.getDaysDue(model.getDueDate()));
        holder.binding.subtaskIcon.setVisibility(View.GONE);
        holder.binding.subtaskCount.setVisibility(View.GONE);
        holder.binding.taskProgressPercent.setVisibility(View.GONE);
        holder.binding.taskProgressBar.setVisibility(View.GONE);

        holder.binding.taskDrag.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder);
            }
            return false;
        });
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
