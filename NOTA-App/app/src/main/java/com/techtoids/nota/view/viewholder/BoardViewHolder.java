package com.techtoids.nota.view.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.adapter.OnItemClickListener;
import com.techtoids.nota.databinding.BoardItemBinding;


public class BoardViewHolder extends RecyclerView.ViewHolder {
    public BoardItemBinding binding;
    private final OnItemClickListener onItemClickListener;

    public BoardViewHolder(BoardItemBinding binding, OnItemClickListener onItemClickListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.onItemClickListener = onItemClickListener;

        binding.getRoot().setOnClickListener(v -> {
            this.onItemClickListener.onItemClick(getBindingAdapterPosition());
        });
    }
}
