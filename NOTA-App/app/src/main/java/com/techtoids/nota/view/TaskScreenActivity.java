package com.techtoids.nota.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.techtoids.nota.R;
import com.techtoids.nota.adapter.TaskListAdapter;
import com.techtoids.nota.adapter.TaskSearchAdapter;
import com.techtoids.nota.databinding.ActivityTaskScreenBinding;
import com.techtoids.nota.helper.BasicHelper;
import com.techtoids.nota.helper.CurrentTaskHelper;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.helper.SwipeNDragHelper;
import com.techtoids.nota.model.BaseTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskScreenActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ActivityTaskScreenBinding binding;
    TaskListAdapter adapter;
    SwipeNDragHelper swipeNDragHelper;
    Query query;
    List<BaseTask> tasks;
    List<BaseTask> filteredTasks = new ArrayList<>();
    TaskSearchAdapter taskSearchAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.home.setOnClickListener(v -> {
            finish();
        });

        String boardId = getIntent().getStringExtra("boardId");
        if (boardId != null) {
            System.out.println(boardId);
            query = FirebaseHelper.getTasksCollection()
                    .whereEqualTo("boardId", boardId)
                    .orderBy("order");

            binding.fabLayout.fab.setOnClickListener(v -> {
                if (BasicHelper.isNetworkAvailable(this)) {
                    Intent intent = new Intent(TaskScreenActivity.this, AddTaskActivity.class);
                    intent.putExtra("boardId", boardId);
                    intent.putExtra("isParent", true);
                    intent.putExtra("isNew", true);
                    int lastOrder = adapter.getItemCount() > 0 ? adapter.getItem(adapter.getItemCount() - 1).getOrder() + 1 : 0;
                    intent.putExtra("order", lastOrder);
                    startActivity(intent);
                }
            });


            swipeNDragHelper = new SwipeNDragHelper(this, 150, binding.taskList) {
                @Override
                protected void instantiateSwipeButton(RecyclerView.ViewHolder viewHolder, List<SwipeUnderlayButton> swipeUnderlayButtons) {
                    swipeUnderlayButtons.add(
                            new SwipeUnderlayButton(
                                    TaskScreenActivity.this,
                                    "Delete",
                                    R.drawable.delete,
                                    30,
                                    0,
                                    ContextCompat.getColor(TaskScreenActivity.this, R.color.error),
                                    SwipeDirection.LEFT,
                                    TaskScreenActivity.this::onItemDelete
                            )
                    );
                    swipeUnderlayButtons.add(
                            new SwipeUnderlayButton(
                                    TaskScreenActivity.this,
                                    "Edit",
                                    R.drawable.folder,
                                    30,
                                    0,
                                    ContextCompat.getColor(TaskScreenActivity.this, R.color.warning),
                                    SwipeDirection.LEFT,
                                    TaskScreenActivity.this::onItemMove
                            )
                    );
                }

                public void onDrag(int oldPosition, int newPosition) {
                    if (BasicHelper.isNetworkAvailable(TaskScreenActivity.this)) {
                        System.out.println("called " + oldPosition + " - " + newPosition);
//                    Collections.swap(adapter.getSnapshots(), oldPosition, newPosition);
                        adapter.notifyItemMoved(oldPosition, newPosition);
                        WriteBatch batch = FirebaseHelper.getDb().batch();
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            BaseTask task = adapter.getItem(i);
                            int position = i;
                            if (i == oldPosition) position = newPosition;
                            else if (i == newPosition) position = oldPosition;
                            if (task.getOrder() != position)
                                batch.update(FirebaseHelper.getTasksCollection().document(task.getTaskId()), "order", position);
                        }
                        batch.commit()
                                .addOnSuccessListener(unused -> {
                                    showSnackbar("Updated Order");
                                    reattachRecyclerView();
                                })
                                .addOnFailureListener(e -> {
                                    showSnackbar("Error Updating");
                                });
                    }
                }
            };

            binding.searchBar.setOnQueryTextListener(this);
            reattachRecyclerView();
        }
    }

    private void reattachRecyclerView() {
        FirestoreRecyclerOptions<BaseTask> options = new FirestoreRecyclerOptions.Builder<BaseTask>().setLifecycleOwner(this).setQuery(query, BaseTask.class).build();
        if (adapter != null)
            adapter.stopListening();
        tasks = options.getSnapshots();
        adapter = new TaskListAdapter(options, this::onItemClick, binding.noRecordsLayout.emptyView);
        binding.taskList.setAdapter(adapter);
        adapter.setItemTouchHelper(swipeNDragHelper.getItemTouchHelper());
    }

    private void onItemMove(int position) {
        if (BasicHelper.isNetworkAvailable(this)) {
            BaseTask task = adapter.getItem(position);
            CurrentTaskHelper.instance.setTaskData(task);
            Intent intent = new Intent(TaskScreenActivity.this, MoveBoardActivity.class);
            intent.putExtra("boardId", task.getBoardId());
            startActivity(intent);
        }
    }

    private void onItemDelete(int position) {
        if (BasicHelper.isNetworkAvailable(this)) {
            BaseTask task = adapter.getItem(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete " + task.getTitle() + "? This will delete all inner tasks.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        FirebaseHelper
                                .getTasksCollection()
                                .document(task.getTaskId())
                                .delete()
                                .addOnSuccessListener(aVoid -> showSnackbar("Deleted " + task.getTitle()))
                                .addOnFailureListener(e -> showSnackbar("Error deleting board"));
                        reattachRecyclerView();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.notifyDataSetChanged();
    }

    private void onItemClick(int position) {
        BaseTask taskData = adapter.getItem(position);
        onItemClick(taskData);
    }

    private void onItemClick(BaseTask taskData) {
        Intent intent = new Intent(TaskScreenActivity.this, ViewTaskActivity.class);
        intent.putExtra("boardId", taskData.getBoardId());
        intent.putExtra("taskId", taskData.getTaskId());
        intent.putExtra("isParent", true);
        startActivity(intent);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onSearch(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        onSearch(newText);
        return true;
    }

    public void onSearchItemClick(int position) {
        onItemClick(taskSearchAdapter.getItem(position));
    }

    public void onSearch(String text) {
        if (text.length() > 0) {
            filteredTasks = tasks.stream().filter(task -> task.contains(text)).collect(Collectors.toList());
            System.out.println(filteredTasks);
            taskSearchAdapter = new TaskSearchAdapter(filteredTasks, this::onSearchItemClick);
            binding.taskList.setAdapter(taskSearchAdapter);
        } else {
            reattachRecyclerView();
        }
    }
}