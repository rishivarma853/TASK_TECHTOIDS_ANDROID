package com.techtoids.nota.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.techtoids.nota.R;
import com.techtoids.nota.adapter.AttachmentAdapter;
import com.techtoids.nota.adapter.ChildTaskListAdapter;
import com.techtoids.nota.databinding.ActivityViewTaskBinding;
import com.techtoids.nota.databinding.AttachmentItemBinding;
import com.techtoids.nota.helper.CurrentTaskHelper;
import com.techtoids.nota.helper.FileHelper;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.helper.SwipeNDragHelper;
import com.techtoids.nota.model.BaseTask;
import com.techtoids.nota.model.Helper;
import com.techtoids.nota.model.TaskStatus;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ViewTaskActivity extends AppCompatActivity implements AttachmentAdapter.OnItemClickListener, OnMapReadyCallback {
    ActivityViewTaskBinding binding;
    AttachmentAdapter attachmentAdapter;
    ChildTaskListAdapter childTaskListAdapter;
    boolean isFullyVisible = false;
    BaseTask task = new BaseTask();
    DocumentReference documentReference;
    SwipeNDragHelper swipeNDragHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.taskHeaderLayout.header.setText("Task");
        binding.taskHeaderLayout.save.setText("Edit");
        updateUI();

        binding.taskDescription.setMaximumHeight(getMaxDescriptionHeight());
        binding.taskDescription.disable();
        attachmentAdapter = new AttachmentAdapter(getAttachmentList(), this);
        binding.attachmentList.setAdapter(attachmentAdapter);

        childTaskListAdapter = new ChildTaskListAdapter(getChildTaskList(), position -> {
            BaseTask taskData = getChildTaskList().get(position);
            Intent intent = new Intent(ViewTaskActivity.this, ViewTaskActivity.class);
            intent.putExtra("boardId", taskData.getBoardId());
            String taskId = getIntent().getStringExtra("taskId");
            intent.putExtra("BaseTaskId", taskId);
            intent.putExtra("taskId", taskData.getTaskId());
            intent.putExtra("isParent", false);
            startActivity(intent);
        });
        binding.childTaskList.setAdapter(childTaskListAdapter);


        binding.taskStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TaskStatus.values()));
        binding.taskStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TaskStatus taskStatus = TaskStatus.values()[position];
                if (task.getTaskStatus() != taskStatus) {
                    Task<Void> updateTask;
                    boolean isParent = getIntent().getBooleanExtra("isParent", false);
                    if (isParent) {
                        task.setUpdatedAt(Helper.getUTCDate());
                        updateTask = documentReference.update("taskStatus", taskStatus);
                    } else {
                        updateTask = documentReference.update("childTasks", FieldValue.arrayRemove(task));
                    }
                    updateTask
                            .addOnSuccessListener(unused -> {
                                if (isParent) {
                                    showSnackbar("Updated progress");
                                    updateUI();
                                } else {
                                    task.setTaskStatus(taskStatus);
                                    task.setUpdatedAt(Helper.getUTCDate());
                                    documentReference.update("childTasks", FieldValue.arrayUnion(task))
                                            .addOnSuccessListener(v -> {
                                                showSnackbar("Updated progress");
                                                updateUI();
                                            }).addOnFailureListener(e -> {
                                                showSnackbar("Error updating status");
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                showSnackbar("Error updating status");
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.taskHeaderLayout.home.setOnClickListener(v -> onBackPress());
        binding.taskHeaderLayout.save.setOnClickListener(v -> onEditClick());
        binding.taskTitle.setEnabled(false);
        binding.taskTitle.setTextColor(getResources().getColor(R.color.black));

        binding.seeMore.setOnClickListener(v -> {
            if (isFullyVisible) {
                binding.seeMore.setText("See More");
                binding.taskDescription.setMaximumHeight(getMaxDescriptionHeight());
            } else {
                binding.seeMore.setText("See Less");
                binding.taskDescription.setMaximumHeight(-1);
            }
            isFullyVisible = !isFullyVisible;
        });

        binding.fabLayout.fab.setOnClickListener(v -> {
            String taskId = getIntent().getStringExtra("taskId");
            String boardId = getIntent().getStringExtra("boardId");
            Intent intent = new Intent(ViewTaskActivity.this, AddTaskActivity.class);
            intent.putExtra("boardId", boardId);
            intent.putExtra("taskId", taskId);
            intent.putExtra("isNew", true);
            int lastOrder = getChildTaskList().size() > 0 ? getChildTaskList().get(getChildTaskList().size() - 1).getOrder() + 1 : 0;
            intent.putExtra("order", lastOrder);
            startActivity(intent);
        });


        swipeNDragHelper = new SwipeNDragHelper(this, 150, binding.childTaskList) {
            @Override
            protected void instantiateSwipeButton(RecyclerView.ViewHolder viewHolder, List<SwipeUnderlayButton> swipeUnderlayButtons) {
                swipeUnderlayButtons.add(
                        new SwipeUnderlayButton(
                                ViewTaskActivity.this,
                                "Delete",
                                R.drawable.delete,
                                30,
                                0,
                                ContextCompat.getColor(ViewTaskActivity.this, R.color.error),
                                SwipeDirection.LEFT,
                                ViewTaskActivity.this::onItemDelete
                        )
                );
                swipeUnderlayButtons.add(
                        new SwipeUnderlayButton(
                                ViewTaskActivity.this,
                                "Edit",
                                R.drawable.folder,
                                30,
                                0,
                                ContextCompat.getColor(ViewTaskActivity.this, R.color.warning),
                                SwipeDirection.LEFT,
                                ViewTaskActivity.this::onItemMove
                        )
                );
            }

            public void onDrag(int oldPosition, int newPosition) {
                System.out.println("called " + oldPosition + " - " + newPosition);
                List<BaseTask> childTaskList = getChildTaskList();
                childTaskList.add(newPosition, childTaskList.remove(oldPosition));
                childTaskListAdapter.notifyItemMoved(oldPosition, newPosition);
                for (int i = 0; i < childTaskListAdapter.getItemCount(); i++) {
                    BaseTask task = childTaskList.get(i);
                    System.out.println(task);
                    if (task.getOrder() != i)
                        task.setOrder(i);
                }
                FirebaseHelper.getTasksCollection()
                        .document(task.getTaskId())
                        .update("childTasks", getChildTaskList())
                        .addOnSuccessListener(unused -> {
                            showSnackbar("Updated Order");
                        })
                        .addOnFailureListener(e -> {
                            showSnackbar("Error Updating");
                        });
            }
        };

        childTaskListAdapter.setItemTouchHelper(swipeNDragHelper.getItemTouchHelper());
    }

    private void onItemMove(int i) {
        BaseTask task1 = getChildTaskList().get(i);
        CurrentTaskHelper.instance.setTaskData(task1);
        Intent intent = new Intent(ViewTaskActivity.this, MoveTaskActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        startActivity(intent);
    }

    private void onItemDelete(int i) {
        BaseTask task1 = getChildTaskList().get(i);
        String taskId = getIntent().getStringExtra("taskId");
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete " + task1.getTitle() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    FirebaseHelper.getTasksCollection().document(taskId).update("childTasks", FieldValue.arrayRemove(task1))
                            .addOnSuccessListener(unused -> {
                                showSnackbar("Deleted " + task1.getTitle());
                                getChildTaskList().remove(i);
                                childTaskListAdapter.notifyItemRemoved(i);
                                updateUI();
                            })
                            .addOnFailureListener(e -> {
                                showSnackbar("Error deleting " + task1.getTitle());
                            });
                })
                .setNegativeButton("No", null)
                .show();

    }


    @Override
    protected void onStart() {
        super.onStart();

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String description = task.getDescription();
        if (description != null)
            binding.taskDescription.setHtml(description);
    }

    private int getMaxDescriptionHeight() {
        Point size = new Point();
        getDisplay().getRealSize(size);
        return size.y / 3;
    }

    private String getContent() {
        return binding.taskDescription.getHtml().trim();
    }

    private List<String> getAttachmentList() {
        return task.getAttachmentList();
    }

    private List<BaseTask> getChildTaskList() {
        if (task.getChildTasks() != null) {
            Collections.sort(task.getChildTasks(), Comparator.comparingInt(BaseTask::getOrder));
        }
        return task.getChildTasks();
    }

    public void updateUI() {
        String taskId = getIntent().getStringExtra("taskId");
        String BaseTaskId = getIntent().getStringExtra("BaseTaskId");
        boolean isParent = getIntent().getBooleanExtra("isParent", false);
        if (isParent) {
            binding.fabLayout.fab.setVisibility(View.GONE);
        } else {
            binding.fabLayout.fab.setVisibility(View.VISIBLE);
        }
        System.out.println(isParent);
        if (taskId != null) {
            documentReference = FirebaseHelper.getTasksCollection()
                    .document(BaseTaskId == null ? taskId : BaseTaskId);
            documentReference.get()
                    .addOnSuccessListener(documentSnapshotTask -> {
                        BaseTask BaseTask = documentSnapshotTask.toObject(BaseTask.class);
                        if (isParent) {
                            task = BaseTask;
                        } else if (BaseTask.getChildTasks() != null) {
                            task = BaseTask.getChildTasks().stream()
                                    .filter(temp -> temp.getTaskId().equals(taskId))
                                    .collect(Collectors.toList()).get(0);
                        }
                        System.out.println(task.getTaskStatus());
                        System.out.println(task.getTaskStatus().ordinal());
                        binding.taskStatus.setSelection(task.getTaskStatus().ordinal());
                        binding.taskTitle.setText(task.getTitle());
                        binding.taskDescription.setHtml(task.getDescription());
                        binding.dueDate.setText(getFormattedDate(task.getDueDate()));
                        binding.lastUpdated.setText("Last Updated On: " + getFormattedDate(task.getUpdatedAt()));
                        attachmentAdapter.updateList(getAttachmentList());
                        childTaskListAdapter.updateList(getChildTaskList());
                        if (task.getLatitude() > 0 && task.getLatitude() > 0) {
                            binding.mapLayout.setVisibility(View.VISIBLE);
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            mapFragment.getMapAsync(this);
                        } else {
                            binding.mapLayout.setVisibility(View.GONE);
                        }
                        refreshAttachmentHeader();

                    });
        }

        refreshAttachmentHeader();
    }

    private void refreshAttachmentHeader() {
        if (getAttachmentList().size() == 0) {
            binding.attachmentHeader.setVisibility(View.GONE);
        } else {
            binding.attachmentHeader.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onItemClick(int position, AttachmentItemBinding binding) {
        String attachment = getAttachmentList().get(position);
        openDocument(Uri.parse(attachment));
    }

    @Override
    public void onCancelClick(int position, AttachmentItemBinding binding) {
        getAttachmentList().remove(position);
        attachmentAdapter.notifyItemRemoved(position);
    }

    private void onEditClick() {
        Intent intent = new Intent(ViewTaskActivity.this, AddTaskActivity.class);
        String taskId = getIntent().getStringExtra("taskId");
        String boardId = getIntent().getStringExtra("boardId");
        intent.putExtra("boardId", boardId);
        intent.putExtra("taskId", taskId);
        intent.putExtra("isParent", true);
        startActivity(intent);
    }

    private void onBackPress() {
        finish();
    }

    public String getFormattedDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a");
        return formatter.format(date.getTime());
    }


    private void openDocument(Uri documentUri) {
        if (documentUri != null) {
            startActivity(FileHelper.getOpenDocumentIntent(documentUri));
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (task.getLatitude() > 0 && task.getLatitude() > 0) {
            googleMap.clear();
            LatLng latLng = new LatLng(task.getLatitude(), task.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(task.getTitle()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        }
    }
}