package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.ActivityListItemBinding;
import com.techtoids.nota.model.BaseTask;
import com.techtoids.nota.model.Board;

import java.util.List;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ViewHolder> {
    private final List<BaseTask> baseTasks;
    private final List<Board> boards;
    private final OnItemClickListener onItemClickListener;

    public ActivityListAdapter(List<BaseTask> baseTasks, OnItemClickListener onItemClickListener) {
        this.baseTasks = baseTasks;
        boards = null;
        this.onItemClickListener = onItemClickListener;
    }

    public ActivityListAdapter(OnItemClickListener onItemClickListener, List<Board> boards) {
        this.boards = boards;
        baseTasks = null;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ActivityListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (baseTasks != null) {
            BaseTask task = baseTasks.get(position);
            holder.binding.title.setText(task.getTitle());
        } else {
            Board board = boards.get(position);
            holder.binding.title.setText(board.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return baseTasks == null ? boards.size() : baseTasks.size();
    }

    public BaseTask getTaskItem(int position) {
        return baseTasks != null ? baseTasks.get(position) : null;
    }

    public Board getBoardItem(int position) {
        return boards != null ? boards.get(position) : null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ActivityListItemBinding binding;

        public ViewHolder(ActivityListItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            this.binding.getRoot().setOnClickListener(v -> {
                System.out.println("clicked");
                onItemClickListener.onItemClick(getBindingAdapterPosition());
            });
        }
    }
}
