package com.techtoids.nota.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.techtoids.nota.R;
import com.techtoids.nota.adapter.BoardListAdapter;
import com.techtoids.nota.databinding.ActivityHomeScreenBinding;
import com.techtoids.nota.databinding.BoardDialogBoxBinding;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.helper.SwipeHelper;
import com.techtoids.nota.model.Board;

import java.util.Date;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {

    ActivityHomeScreenBinding binding;

    FirebaseFirestore db;
    CollectionReference collectionReference;
    Query query;
    BoardListAdapter adapter;
    SwipeHelper swipeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        if (!FirebaseHelper.isSignedIn()) {
            startSignIn();
            return;
        }

        FirebaseUser user = FirebaseHelper.getCurrentUser();
        collectionReference = db.collection("boards");

        query = collectionReference.whereEqualTo("userId", user.getUid()).orderBy("updatedAt",
                Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Board> options = new FirestoreRecyclerOptions.Builder<Board>()
                .setQuery(query, Board.class).setLifecycleOwner(this).build();
        adapter = new BoardListAdapter(options, this::onItemClick, binding.noRecordsLayout.emptyView);
        binding.boardList.setAdapter(adapter);

        binding.fabLayout.fab.setOnClickListener(view -> {
            Board board = new Board();
            board.setUserId(user.getUid());
            showDialog(board, false);
        });

        swipeHelper = new SwipeHelper(this, 150, binding.boardList) {
            @Override
            protected void instantiateSwipeButton(RecyclerView.ViewHolder viewHolder, List<SwipeUnderlayButton> swipeUnderlayButtons) {
                swipeUnderlayButtons.add(
                        new SwipeUnderlayButton(
                                HomeScreenActivity.this,
                                "Delete",
                                R.drawable.delete,
                                30,
                                0,
                                ContextCompat.getColor(HomeScreenActivity.this, R.color.error),
                                SwipeDirection.LEFT,
                                HomeScreenActivity.this::onItemDelete
                        )
                );
                swipeUnderlayButtons.add(
                        new SwipeUnderlayButton(
                                HomeScreenActivity.this,
                                "Edit",
                                R.drawable.edit,
                                30,
                                0,
                                ContextCompat.getColor(HomeScreenActivity.this, R.color.warning),
                                SwipeDirection.LEFT,
                                HomeScreenActivity.this::onItemEdit
                        )
                );
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        boolean showSnackbar = intent.getBooleanExtra("showSnackbar", false);
        if (showSnackbar) {
            FirebaseUser user = FirebaseHelper.getCurrentUser();
            if (user != null) {
                String name = user.getDisplayName();
                showSnackbar("Welcome " + name);
            }
        } else if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    private void showDialog(Board board, boolean edit) {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setCancelable(false);

        BoardDialogBoxBinding boardDialogBoxBinding = BoardDialogBoxBinding.inflate(dialog.getLayoutInflater());

        dialog.setContentView(boardDialogBoxBinding.getRoot());
        if (board.getTitle() != null) {
            boardDialogBoxBinding.dialogHeader.setText("Update Board Title");
            boardDialogBoxBinding.input.getEditText().setText(board.getTitle());
        }
        boardDialogBoxBinding.btnClose.setOnClickListener(view -> {
            dialog.dismiss();
        });
        boardDialogBoxBinding.btnYes.setOnClickListener(view -> {
            String value = String.valueOf(boardDialogBoxBinding.input.getEditText().getText());
            if (value.isEmpty()) {
                Toast.makeText(HomeScreenActivity.this, "Board Name empty", Toast.LENGTH_SHORT).show();
                return;
            }
            board.setTitle(value);
            board.setUpdatedAt(new Date());
            if (edit) {
                onUpdate(board);
            } else {
                onAdd(board);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onItemDelete(int position) {
        Board board = adapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete " + board.getTitle() + "? This will delete all inner tasks.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    onDelete(board);
                })
                .setNegativeButton("No", null)
                .show();

    }

    private void onItemEdit(int position) {
        Board board = adapter.getItem(position);
        showDialog(board, true);
    }

    public void onItemClick(Board board) {
        navigateToBoard(board.getBoardId());
    }

    public void onDelete(Board board) {
        collectionReference
                .document(board.getBoardId())
                .delete()
                .addOnSuccessListener(aVoid -> showSnackbar("Deleted " + board.getTitle()))
                .addOnFailureListener(e -> {
                    showSnackbar("Error deleting board");
                });
    }

    private void onUpdate(Board board) {
        collectionReference
                .document(board.getBoardId())
                .update("title", board.getTitle())
                .addOnSuccessListener(aVoid -> showSnackbar("Updated " + board.getTitle()))
                .addOnFailureListener(e -> {
                    showSnackbar("Error updating board");
                });
    }

    private void onAdd(Board board) {
        collectionReference
                .document(board.getBoardId())
                .set(board)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Added " + board.getTitle());
                    navigateToBoard(board.getBoardId());
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Error adding board");
                });
    }

    private void navigateToBoard(String boardId) {
        Intent intent = new Intent(HomeScreenActivity.this, TaskScreenActivity.class);
        intent.putExtra("boardId", boardId);
        startActivity(intent);
    }

    private void startSignIn() {
        Intent intent = new Intent(HomeScreenActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }
}
