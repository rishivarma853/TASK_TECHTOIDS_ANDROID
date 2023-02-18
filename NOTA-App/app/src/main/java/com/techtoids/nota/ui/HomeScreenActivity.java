package com.techtoids.nota.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

import com.techtoids.nota.R;
import com.techtoids.nota.adapter.BoardListAdapter;
import com.techtoids.nota.databinding.ActivityHomeScreenBinding;
import com.techtoids.nota.databinding.BoardDialogBoxBinding;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.model.Board;

public class HomeScreenActivity extends AppCompatActivity {

    ActivityHomeScreenBinding binding;

    FirebaseFirestore db;
    CollectionReference collectionReference;
    Query query;
    BoardListAdapter adapter;

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
        adapter = new BoardListAdapter(options, this::onItemClick);
        binding.boardList.setAdapter(adapter);

        binding.fabLayout.fab.setOnClickListener(view -> {
            showDialog();
        });
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
                Snackbar.make(binding.getRoot(), "Welcome " + name, Snackbar.LENGTH_LONG).show();
            }
        } else if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    private void showDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setCancelable(false);

        BoardDialogBoxBinding boardDialogBoxBinding = BoardDialogBoxBinding.inflate(dialog.getLayoutInflater());

        dialog.setContentView(boardDialogBoxBinding.getRoot());

        boardDialogBoxBinding.btnClose.setOnClickListener(view -> {
            dialog.dismiss();
        });
        boardDialogBoxBinding.btnYes.setOnClickListener(view -> {
            String value = String.valueOf(boardDialogBoxBinding.input.getEditText().getText());
            if (value.isEmpty()) {
                Toast.makeText(HomeScreenActivity.this, "Board Name empty", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Board board = new Board();
            board.setTitle(value);
            board.setUpdatedAt(new Date());
            FirebaseUser user = FirebaseHelper.getCurrentUser();
            board.setUserId(user.getUid());
            collectionReference.document(board.getBoardId()).set(board)
                    .addOnSuccessListener(aVoid -> {
                        navigateToBoard(board.getBoardId());
                    })
                    .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));
        });

        dialog.show();
    }

    private void navigateToBoard(String boardId) {
        Intent intent = new Intent(HomeScreenActivity.this, TaskScreenActivity.class);
        intent.putExtra("boardId", boardId);
        startActivity(intent);
    }

    public void onItemClick(Board board) {
        navigateToBoard(board.getBoardId());
    }

    private void startSignIn() {
        Intent intent = new Intent(HomeScreenActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
