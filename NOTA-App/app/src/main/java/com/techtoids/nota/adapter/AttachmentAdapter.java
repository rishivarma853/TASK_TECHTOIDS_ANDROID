package com.techtoids.nota.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techtoids.nota.databinding.AttachmentItemBinding;

import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    private final OnItemClickListener onItemClickListener;
    public boolean shouldShowCancel = false;
    private List<String> attachments;

    public AttachmentAdapter(List<String> attachments, OnItemClickListener onItemClickListener) {
        this.attachments = attachments;

        this.onItemClickListener = onItemClickListener;
    }

    public void updateList(List<String> attachments){
        this.attachments = attachments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AttachmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String attachment = attachments.get(position);
        System.out.println(attachment);
        if (shouldShowCancel) {
            holder.binding.cancel.setVisibility(View.VISIBLE);
        } else {
            holder.binding.cancel.setVisibility(View.GONE);
        }
        holder.binding.title.setText(Uri.parse(attachment).getLastPathSegment());
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, AttachmentItemBinding binding);

        void onCancelClick(int position, AttachmentItemBinding binding);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AttachmentItemBinding binding;

        public ViewHolder(@NonNull AttachmentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                onItemClickListener.onItemClick(getAdapterPosition(), binding);
            });
            binding.cancel.setOnClickListener(v -> {
                onItemClickListener.onCancelClick(getAdapterPosition(), binding);
            });
        }
    }
}
