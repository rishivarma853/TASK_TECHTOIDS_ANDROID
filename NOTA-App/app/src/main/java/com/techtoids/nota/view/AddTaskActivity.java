package com.techtoids.nota.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtoids.nota.R;
import com.techtoids.nota.adapter.AttachmentAdapter;
import com.techtoids.nota.databinding.ActivityAddTaskBinding;
import com.techtoids.nota.databinding.AttachmentItemBinding;
import com.techtoids.nota.helper.CurrentTaskHelper;
import com.techtoids.nota.helper.FileHelper;
import com.techtoids.nota.helper.FirebaseHelper;
import com.techtoids.nota.helper.SimpleTextWatcher;
import com.techtoids.nota.model.BaseTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AddTaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AttachmentAdapter.OnItemClickListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    ActivityAddTaskBinding binding;
    DatePickerDialog datePickerDialog;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("attachments");
    AttachmentAdapter adapter;
    private final ActivityResultLauncher<Intent> documentPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() == null) return;
            Uri uri = result.getData().getData();
            String filename = FileHelper.getNewFileName(this, uri);
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                StorageReference documentRef = storageRef.child(filename);
                UploadTask uploadTask = documentRef.putStream(inputStream);
                startAnimation();
                uploadTask
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return documentRef.getDownloadUrl();
                        })
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                getAttachmentList().add(downloadUri.toString());
                                System.out.println(getAttachmentList());
                                adapter.notifyItemInserted(getAttachmentList().size() - 1);
                            }
                            stopAnimation();
                        })
                        .addOnFailureListener(e -> stopAnimation());
            } catch (FileNotFoundException e) {
                stopAnimation();
                throw new RuntimeException(e);
            }
        }
    });
    Calendar date = Calendar.getInstance();
    Menu menu;
    boolean isFullyVisible = false;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        String boardId = getIntent().getStringExtra("boardId");
        String taskId = getIntent().getStringExtra("taskId");
        String baseTaskId = getIntent().getStringExtra("baseTaskId");
        int order = getIntent().getIntExtra("order", 0);
        boolean isParent = getIntent().getBooleanExtra("isParent", false);
        boolean isNew = getIntent().getBooleanExtra("isNew", false);
        CurrentTaskHelper.instance.setTaskData(new BaseTask());
        CurrentTaskHelper.instance.taskData.setBoardId(boardId);
        CurrentTaskHelper.instance.taskData.setUserId(FirebaseHelper.getCurrentUser().getUid());
        CurrentTaskHelper.instance.taskData.setOrder(order);
        setLocation();
        binding.taskHeaderLayout.header.setText((isNew ? "Add" : "Update") + " "+(isParent?"":"Sub ")+"Task");
        if (taskId != null && !isNew) {
            System.out.println("called");
            FirebaseHelper.getTasksCollection()
                    .document(baseTaskId == null ? taskId : baseTaskId).get()
                    .addOnSuccessListener(documentSnapshotTask -> {
                        BaseTask task = documentSnapshotTask.toObject(BaseTask.class);
                        if (!isParent) {
                            task = task.getChildTasks().stream()
                                    .filter(temp -> temp.getTaskId().equals(taskId))
                                    .collect(Collectors.toList()).get(0);
                        }
                        CurrentTaskHelper.instance.setTaskData(task);
                        adapter.updateList(getAttachmentList());
                        adapter.shouldShowCancel = true;
                        updateUI();
                        System.out.println("found");
                    }).addOnFailureListener(e -> {
                        System.out.println(e.getLocalizedMessage());
                    });
        }

        binding.taskDescription.setMaximumHeight(getMaxDescriptionHeight());
        binding.taskDescription.disable();
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
        binding.dueDate.setText(getFormattedDate());

        adapter = new AttachmentAdapter(getAttachmentList(), this);
        binding.attachmentList.setAdapter(adapter);
        adapter.shouldShowCancel = true;

        binding.dueDate.setOnClickListener(v -> {
            showDatePicker();
        });
        binding.editDescription.setOnClickListener(v -> {
            Intent intent = new Intent(AddTaskActivity.this, TaskDescriptionActivity.class);
            startActivity(intent);
        });
        binding.addAttachment.setOnClickListener(v -> {
            addDocument();
        });

        binding.taskHeaderLayout.home.setOnClickListener(v -> onBackPress());
        binding.taskHeaderLayout.save.setOnClickListener(v -> onSaveClick());
        binding.taskTitle.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(String text) {
                CurrentTaskHelper.instance.taskData.setTitle(text);
            }
        });

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String description = CurrentTaskHelper.instance.taskData.getDescription();
        if (description != null)
            binding.taskDescription.setHtml(description);
    }

    @Override
    public void onBackPressed() {
        onBackPress();
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
        return CurrentTaskHelper.instance.taskData.getAttachmentList();
    }

    public void updateUI() {
        System.out.println(CurrentTaskHelper.instance.taskData);
        binding.taskTitle.setText(CurrentTaskHelper.instance.taskData.getTitle());
        String description = CurrentTaskHelper.instance.taskData.getDescription();
        if (description != null)
            binding.taskDescription.setHtml(description);
        if (menu != null)
            menu.findItem(R.id.edit).setTitle("Save");
        binding.taskTitle.setEnabled(true);
        binding.taskTitle.setTextColor(getResources().getColor(R.color.black));
        binding.editDescription.setVisibility(View.VISIBLE);
        binding.addAttachment.setVisibility(View.VISIBLE);

    }

    @Override
    public void onItemClick(int position, AttachmentItemBinding binding) {
        String attachment = getAttachmentList().get(position);
        openDocument(Uri.parse(attachment));
    }

    @Override
    public void onCancelClick(int position, AttachmentItemBinding binding) {
        getAttachmentList().remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void onSaveClick() {
        BaseTask taskData = CurrentTaskHelper.instance.taskData;
        System.out.println(taskData.getDescription());
        if (taskData.getTitle() == null || taskData.getTitle().trim().length() == 0) {
            showDialog("Title cannot be empty");
        } else if (taskData.getDescription() == null || taskData.getDescription().isEmpty()) {
            showDialog("Description cannot be empty");
        } else if (taskData.getDueDate() == null) {
            showDialog("Due date not selected");
        } else if (taskData.getDueDate().before(new Date())) {
            showDialog("Due date cant be past");
        } else {
            taskData.setUpdatedAt(new Date());
            boolean isParent = getIntent().getBooleanExtra("isParent", false);
            boolean isNew = getIntent().getBooleanExtra("isNew", false);
            DocumentReference documentReference = FirebaseHelper.getTasksCollection()
                    .document(taskData.getTaskId());
            Task<Void> task = null;
            if (isNew) {
                if (isParent) {
                    task = documentReference.set(taskData);
                } else {
                    String taskId = getIntent().getStringExtra("taskId");
                    String baseTaskId = getIntent().getStringExtra("baseTaskId");
                    documentReference = FirebaseHelper.getTasksCollection().document(baseTaskId == null ? taskId : baseTaskId);
                    task = documentReference.update("childTasks", FieldValue.arrayUnion(taskData));
                }
            } else if (isParent) {
                task = documentReference.set(taskData);
            } else {
                String taskId = getIntent().getStringExtra("taskId");
                String baseTaskId = getIntent().getStringExtra("baseTaskId");
                documentReference = FirebaseHelper.getTasksCollection().document(baseTaskId == null ? taskId : baseTaskId);
                task = documentReference.update("childTasks", FieldValue.arrayRemove(CurrentTaskHelper.instance.getOriginalData()));
            }
            DocumentReference finalDocumentReference = documentReference;
            task.addOnSuccessListener(aVoid -> {
                        if (!isParent && !isNew) {
                            finalDocumentReference
                                    .update("childTasks", FieldValue.arrayUnion((BaseTask) taskData))
                                    .addOnSuccessListener(unused -> {
                                        finish();
                                        CurrentTaskHelper.instance.setTaskData(null);
                                    })
                                    .addOnFailureListener(e -> {
                                        showSnackbar("Error adding board");
                                    });
                        } else {
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar("Error adding board");
                    });
        }

        updateUI();
        adapter.notifyDataSetChanged();
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .show();
    }

    private void onBackPress() {
        if (CurrentTaskHelper.instance.hasChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Changes Detected")
                    .setMessage("Do you want to discard changes and go back?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        System.out.println("yes");
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return;
        }

        finish();
    }

    private void showDatePicker() {
        if (!datePickerDialog.isShowing())
            datePickerDialog.show();
    }

    public String getFormattedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
        return formatter.format(date.getTime());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.HOUR),
                Calendar.getInstance().get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
        date.set(Calendar.MINUTE, minute);

        binding.dueDate.setText(getFormattedDate());
        CurrentTaskHelper.instance.taskData.setDueDate(date.getTime());
    }

    private void addDocument() {
        documentPickerLauncher.launch(FileHelper.getSelectDocumentIntent());
    }

    private void openDocument(Uri documentUri) {
        if (documentUri != null) {
            startActivity(FileHelper.getOpenDocumentIntent(documentUri));
        }
    }

    public void startAnimation() {
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        binding.progressBarHolder.setAnimation(inAnimation);
        binding.progressBarHolder.setVisibility(View.VISIBLE);
        binding.taskHeaderLayout.save.setEnabled(false);
    }

    public void stopAnimation() {
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        binding.progressBarHolder.setAnimation(outAnimation);
        binding.progressBarHolder.setVisibility(View.GONE);
        binding.taskHeaderLayout.save.setEnabled(true);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    void onPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.
                getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                })
                .addOnSuccessListener(this, location -> {
                    System.out.println(location);
                    if (location != null) {
                        CurrentTaskHelper.instance.taskData.setLatitude(location.getLatitude());
                        CurrentTaskHelper.instance.taskData.setLongitude(location.getLongitude());
                    }
                }).addOnFailureListener(e -> {
                    System.out.println(e.getLocalizedMessage());
                });
    }

    private void setLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}