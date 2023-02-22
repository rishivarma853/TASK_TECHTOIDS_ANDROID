package com.techtoids.nota.view.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.adapter.OnItemClickListener;
import com.techtoids.nota.databinding.BoardItemBinding;


public class BoardViewHolder extends RecyclerView.ViewHolder {
    private final OnItemClickListener onItemClickListener;
    public BoardItemBinding binding;

    public BoardViewHolder(BoardItemBinding binding, OnItemClickListener onItemClickListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.onItemClickListener = onItemClickListener;

        binding.getRoot().setOnClickListener(v -> {
            this.onItemClickListener.onItemClick(getBindingAdapterPosition());
        });
    }
}
