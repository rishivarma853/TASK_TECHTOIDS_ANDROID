package ca.lcit22fw.madt.techtoids.android.nota_app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.BoardItemBinding;
import ca.lcit22fw.madt.techtoids.android.nota_app.model.Board;
import ca.lcit22fw.madt.techtoids.android.nota_app.ui.AvatarPlaceholder;
import ca.lcit22fw.madt.techtoids.android.nota_app.ui.BoardViewHolder;

public class BoardListAdapter extends FirestoreRecyclerAdapter<Board, BoardViewHolder> {
    private final OnItemClickListener onItemClickListener;
    public BoardListAdapter(@NonNull FirestoreRecyclerOptions<Board> options, OnItemClickListener onItemClickListener) {
        super(options);
        this.onItemClickListener = onItemClickListener;
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

    public interface OnItemClickListener{
        public void onItemClick(Board model);
    }
}
