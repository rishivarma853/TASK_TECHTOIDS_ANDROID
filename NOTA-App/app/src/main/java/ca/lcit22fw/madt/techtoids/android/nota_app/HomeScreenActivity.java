package ca.lcit22fw.madt.techtoids.android.nota_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Date;

import ca.lcit22fw.madt.techtoids.android.nota_app.adapter.BoardListAdapter;
import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.ActivityHomeScreenBinding;
import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.BoardItemBinding;
import ca.lcit22fw.madt.techtoids.android.nota_app.model.Board;
import ca.lcit22fw.madt.techtoids.android.nota_app.ui.BoardViewHolder;

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
        System.out.println(user.getUid());
        collectionReference = db.collection("boards");

        query = collectionReference.whereEqualTo("userId", user.getUid()).orderBy("updatedAt", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Board> options = new FirestoreRecyclerOptions.Builder<Board>().setQuery(query, Board.class).setLifecycleOwner(this).build();
        adapter = new BoardListAdapter(options, this::onItemClick);
        binding.boardList.setAdapter(adapter);

        binding.fab.setOnClickListener(view -> {

            View mView = getLayoutInflater().inflate(R.layout.alert_input, null);
            final TextInputLayout input = mView.findViewById(R.id.input);

            new AlertDialog.Builder(HomeScreenActivity.this)
                    .setView(mView)
                    .setTitle("Enter new board Name")
                    .setPositiveButton("ADD", (dialog, whichButton) -> {
                        String value = String.valueOf(input.getEditText().getText());
                        Board board = new Board();
                        board.setTitle(value);
                        board.setUpdatedAt(new Date());
                        board.setUserId(user.getUid());
                        collectionReference.document(board.getBoardId()).set(board)
                                .addOnSuccessListener(aVoid -> {
                                    navigateToBoard(board.getBoardId());
                                })
                                .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));

                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).create().show();

        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.notifyDataSetChanged();
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