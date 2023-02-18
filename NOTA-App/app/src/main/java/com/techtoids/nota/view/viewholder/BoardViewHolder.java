package com.techtoids.nota.view.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.BoardItemBinding;


public class BoardViewHolder extends RecyclerView.ViewHolder {
    public BoardItemBinding binding;

    public BoardViewHolder(BoardItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

}
