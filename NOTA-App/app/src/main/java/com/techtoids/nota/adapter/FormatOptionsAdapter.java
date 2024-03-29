package com.techtoids.nota.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.FormatItemBinding;
import com.techtoids.nota.model.FormatOption;

import java.util.List;

public class FormatOptionsAdapter extends RecyclerView.Adapter<FormatOptionsAdapter.ViewHolder> {

    private List<FormatOption> formatOptionList;

//    private final OnItemClickListener onItemClickListener;

    public FormatOptionsAdapter(List<FormatOption> formatOptionList) {
        this.formatOptionList = formatOptionList;
//        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FormatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FormatOption formatOption = formatOptionList.get(position);
        holder.binding.formatItem.setImageResource(formatOption.getIcon());
    }

    @Override
    public int getItemCount() {
        return formatOptionList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, FormatItemBinding binding);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        FormatItemBinding binding;

        public ViewHolder(@NonNull FormatItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.formatItem.setOnClickListener(v -> {
                FormatOption formatOption = formatOptionList.get(getAdapterPosition());
                formatOption.clickListener.onItemClick();
            });
        }
    }
}
