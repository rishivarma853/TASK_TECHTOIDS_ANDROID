package com.techtoids.nota.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.techtoids.nota.R;
import com.techtoids.nota.adapter.BoardListAdapter;
import com.techtoids.nota.adapter.BoardSearchAdapter;
import com.techtoids.nota.databinding.ActivityHomeScreenBinding;
import com.techtoids.nota.databinding.BoardDialogBoxBinding;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.helper.SwipeNDragHelper;
import com.techtoids.nota.model.Board;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HomeScreenActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ActivityHomeScreenBinding binding;
    BoardListAdapter adapter;
    SwipeNDragHelper swipeNDragHelper;
    List<Board> boards;
    List<Board> filteredBoards = new ArrayList<>();
    BoardSearchAdapter boardSearchAdapter = null;
    int sortBy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!FirebaseHelper.isSignedIn()) {
            startSignIn();
            return;
        }

        reattachRecyclerView();

        binding.searchBar.setOnQueryTextListener(this);

        binding.fabLayout.fab.setOnClickListener(view -> {
            Board board = new Board();
            board.setUserId(FirebaseHelper.getCurrentUser().getUid());
            showDialog(board, false);
        });
        binding.sortMenu.setOnClickListener(v -> {
            new AlertDialog.Builder(HomeScreenActivity.this)
                    .setTitle("Sort By")
                    .setSingleChoiceItems(
                            new String[]{
                                    "Title", "Date"
                            },
                            sortBy,
                            (dialog, which) -> {
                                sortBy = which;
                                dialog.dismiss();
                                onFilterUpdate();
                            }
                    )
                    .show();
        });

        swipeNDragHelper = new SwipeNDragHelper(this, 150, binding.boardList) {
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

            @Override
            public void onDrag(int oldPosition, int newPosition) {

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

    private void reattachRecyclerView() {
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        Query query = FirebaseHelper.getBoardsCollection().whereEqualTo("userId", user.getUid()).orderBy("updatedAt", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Board> options = new FirestoreRecyclerOptions.Builder<Board>()
                .setQuery(query, Board.class).setLifecycleOwner(this).build();
        if (adapter != null)
            adapter.stopListening();
        boards = options.getSnapshots();
        adapter = new BoardListAdapter(options, this::onItemClick, binding.noRecordsLayout.emptyView);
        binding.boardList.setAdapter(adapter);
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

    public void onItemClick(int position) {
        Board board = adapter.getItem(position);
        navigateToBoard(board.getBoardId());
    }

    public void onDelete(Board board) {
        FirebaseHelper
                .getBoardsCollection()
                .document(board.getBoardId())
                .delete()
                .addOnSuccessListener(aVoid -> showSnackbar("Deleted " + board.getTitle()))
                .addOnFailureListener(e -> {
                    showSnackbar("Error deleting board");
                });
    }

    private void onUpdate(Board board) {
        FirebaseHelper
                .getBoardsCollection()
                .document(board.getBoardId())
                .update("title", board.getTitle())
                .addOnSuccessListener(aVoid -> showSnackbar("Updated " + board.getTitle()))
                .addOnFailureListener(e -> {
                    showSnackbar("Error updating board");
                });
    }

    private void onAdd(Board board) {
        FirebaseHelper
                .getBoardsCollection()
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        onSearchChange(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        onSearchChange(newText);
        return true;
    }

    public void onSearchChange(String text) {
        if (text.length() > 0) {
            filteredBoards = boards.stream().filter(board -> board.getTitle().toLowerCase().contains(text.trim().toLowerCase())).collect(Collectors.toList());
            System.out.println(filteredBoards);
            boardSearchAdapter = new BoardSearchAdapter(filteredBoards, this::onSearchItemClick);
            binding.boardList.setAdapter(boardSearchAdapter);
        } else {
            reattachRecyclerView();
        }
    }

    private void onSearchItemClick(int position) {
        navigateToBoard(boardSearchAdapter.getItem(position).getBoardId());
    }

    public void onFilterUpdate() {
        Query query = FirebaseHelper.getBoardsCollection()
                .whereEqualTo("userId", FirebaseHelper.getCurrentUser().getUid())
                .orderBy(sortBy == 0 ? "title" : "updatedAt")
                .orderBy("updatedAt", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Board> options = new FirestoreRecyclerOptions.Builder<Board>()
                .setQuery(query, Board.class).setLifecycleOwner(this).build();
        if (adapter != null)
            adapter.stopListening();
        boards = options.getSnapshots();
        adapter = new BoardListAdapter(options, this::onItemClick, binding.noRecordsLayout.emptyView);
        binding.boardList.setAdapter(adapter);
    }
}
