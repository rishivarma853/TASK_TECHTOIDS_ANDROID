package ca.lcit22fw.madt.techtoids.android.nota_app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

import ca.lcit22fw.madt.techtoids.android.nota_app.adapter.BoardListAdapter;
import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.ActivityHomeScreenBinding;
import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.BoardDialogBoxBinding;
import ca.lcit22fw.madt.techtoids.android.nota_app.model.Board;

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

        if (!isSignedIn()) {
            startSignIn();
            return;
        }

        FirebaseUser user = getUser();
        collectionReference = db.collection("boards");

        query = collectionReference.whereEqualTo("userId", user.getUid()).orderBy("updatedAt",
                Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Board> options = new FirestoreRecyclerOptions.Builder<Board>()
                .setQuery(query, Board.class).setLifecycleOwner(this).build();
        adapter = new BoardListAdapter(options, this::onItemClick);
        binding.boardList.setAdapter(adapter);

        binding.fab.setOnClickListener(view -> {
            showDialog();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.notifyDataSetChanged();
    }


    private void showDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        BoardDialogBoxBinding boardDialogBoxBinding = BoardDialogBoxBinding.inflate(dialog.getLayoutInflater());

        dialog.setContentView(boardDialogBoxBinding.getRoot());

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        boardDialogBoxBinding.btnClose.setOnClickListener(view -> {
            dialog.dismiss();
            String value = String.valueOf(boardDialogBoxBinding.editText.getText());
            Board board = new Board();
            board.setTitle(value);
            board.setUpdatedAt(new Date());
            FirebaseUser user = getUser();
            board.setUserId(user.getUid());
            collectionReference.document(board.getBoardId()).set(board)
                    .addOnSuccessListener(aVoid -> {
                        navigateToBoard(board.getBoardId());
                    })
                    .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));
        });

        dialog.show();
    }

    private boolean isSignedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    private FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void navigateToBoard(String boardId) {
        Intent intent = new Intent(HomeScreenActivity.this, TaskScreenActivity.class);
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
