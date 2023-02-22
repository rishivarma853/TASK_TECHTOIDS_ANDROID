package com.techtoids.nota.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.techtoids.nota.adapter.ActivityListAdapter;
import com.techtoids.nota.databinding.ActivityMoveTaskBinding;
import com.techtoids.nota.helper.CurrentTaskHelper;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.model.BaseTask;

import java.util.ArrayList;
import java.util.List;

public class MoveTaskActivity extends AppCompatActivity {
    ActivityMoveTaskBinding binding;
    ActivityListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoveTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String taskId = getIntent().getStringExtra("taskId");
        String boardId = getIntent().getStringExtra("boardId");
        FirebaseHelper.getTasksCollection()
                .whereEqualTo("boardId", boardId)
                .whereNotEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {

                    List<BaseTask> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        BaseTask task1 = document.toObject(BaseTask.class);
                        list.add(task1);
                    }
                    if (list.size() == 0) {
                        Toast.makeText(this, "No other tasks available", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        adapter = new ActivityListAdapter(list, this::onItemClick);
                        binding.taskList.setAdapter(adapter);
                    }
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
            String taskId = getIntent().getStringExtra("taskId");
            System.out.println(taskId);
            BaseTask task = adapter.getTaskItem(position);
            WriteBatch batch = FirebaseHelper.getDb().batch();
            batch.update(FirebaseHelper.getTasksCollection().document(taskId), "childTasks", FieldValue.arrayRemove(CurrentTaskHelper.instance.taskData));
            batch.update(FirebaseHelper.getTasksCollection().document(task.getTaskId()), "childTasks", FieldValue.arrayUnion(CurrentTaskHelper.instance.taskData));
            batch.commit()
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