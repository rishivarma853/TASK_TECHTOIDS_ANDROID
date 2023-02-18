package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.techtoids.nota.databinding.BoardItemBinding;
import com.techtoids.nota.model.Board;
import com.techtoids.nota.view.custom.AvatarPlaceholder;
import com.techtoids.nota.view.viewholder.BoardViewHolder;

public class BoardListAdapter extends FirestoreRecyclerAdapter<Board, BoardViewHolder> {
    private final OnItemClickListener onItemClickListener;

    private final View emptyView;

    public BoardListAdapter(@NonNull FirestoreRecyclerOptions<Board> options, OnItemClickListener onItemClickListener, View emptyView) {
        super(options);
        this.onItemClickListener = onItemClickListener;
        this.emptyView = emptyView;
    }

    @Override
    protected void onBindViewHolder(@NonNull BoardViewHolder holder, int position, @NonNull Board model) {
        holder.binding.boardTitle.setText(model.getTitle());
        holder.binding.boardAvatar.setImageDrawable(new AvatarPlaceholder(model.getTitle(), 50));
        holder.binding.getRoot().setOnClickListener(v -> {
            onItemClickListener.onItemClick(model);
        });
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BoardViewHolder(
                BoardItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false)
        );
    }

    public interface OnItemClickListener {
        public void onItemClick(Board model);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();

        if (getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }

    }
}
