package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.techtoids.nota.databinding.TaskListItemBinding;
import com.techtoids.nota.model.ParentTask;
import com.techtoids.nota.view.viewholder.TaskViewHolder;

public class TaskListAdapter extends FirestoreRecyclerAdapter<ParentTask, TaskViewHolder> {

    public TaskListAdapter(@NonNull FirestoreRecyclerOptions<ParentTask> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull ParentTask model) {

    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(TaskListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
}
