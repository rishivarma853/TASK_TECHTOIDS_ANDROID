package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.BoardItemBinding;
import com.techtoids.nota.model.Board;
import com.techtoids.nota.view.custom.AvatarPlaceholder;
import com.techtoids.nota.view.viewholder.BoardViewHolder;

import java.util.List;

public class BoardSearchAdapter extends RecyclerView.Adapter<BoardViewHolder> {
    private final List<Board> boards;
    private final OnItemClickListener onItemClickListener;
//    private final View emptyView;

    public BoardSearchAdapter(List<Board> boards, OnItemClickListener onItemClickListener) {
        this.boards = boards;
        this.onItemClickListener = onItemClickListener;
//        this.emptyView = emptyView;
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Board model = boards.get(position);
        holder.binding.boardTitle.setText(model.getTitle());
        holder.binding.boardAvatar.setImageDrawable(new AvatarPlaceholder(model.getTitle(), 50));
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    public Board getItem(int position){
        return boards.get(position);
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BoardViewHolder(
                BoardItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                onItemClickListener);
    }
}
