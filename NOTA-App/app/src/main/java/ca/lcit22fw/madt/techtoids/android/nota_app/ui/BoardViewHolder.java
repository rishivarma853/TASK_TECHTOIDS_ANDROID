package ca.lcit22fw.madt.techtoids.android.nota_app.ui;

import androidx.recyclerview.widget.RecyclerView;

import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.BoardItemBinding;

public class BoardViewHolder extends RecyclerView.ViewHolder {
    public BoardItemBinding binding;

    public BoardViewHolder(BoardItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

}
