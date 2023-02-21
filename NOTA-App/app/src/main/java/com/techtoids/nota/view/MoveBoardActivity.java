package com.techtoids.nota.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.techtoids.nota.adapter.ActivityListAdapter;
import com.techtoids.nota.databinding.ActivityMoveTaskBinding;
import com.techtoids.nota.helper.CurrentTaskHelper;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.model.Board;

import java.util.ArrayList;
import java.util.List;

public class MoveBoardActivity extends AppCompatActivity {
    ActivityMoveTaskBinding binding;
    ActivityListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoveTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String boardId = getIntent().getStringExtra("boardId");
        FirebaseHelper.getBoardsCollection()
                .whereNotEqualTo("boardId", boardId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Board> list = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Board board = document.toObject(Board.class);
                            list.add(board);
                        }
                        if (list.size() > 0) {
                            adapter = new ActivityListAdapter(this::onItemClick, list);
                            binding.taskList.setAdapter(adapter);
                            return;
                        }
                    }
                    Toast.makeText(this, "No other boards available", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching tasks", Toast.LENGTH_SHORT).show();
                    finish();
                });
        binding.headerLayout.home.setOnClickListener(v -> finish());
        binding.headerLayout.save.setVisibility(View.GONE);
        binding.headerLayout.header.setText("Move Task");
    }

    private void onItemClick(int position) {
        System.out.println(position);
        if (adapter != null) {
            String taskId = CurrentTaskHelper.instance.taskData.getTaskId();
            Board board = adapter.getBoardItem(position);
            FirebaseHelper.getTasksCollection()
                    .document(taskId).update("boardId", board.getBoardId())
                    .addOnCompleteListener(task1 -> {
                        Toast.makeText(this, "Moved task successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error moving task", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}