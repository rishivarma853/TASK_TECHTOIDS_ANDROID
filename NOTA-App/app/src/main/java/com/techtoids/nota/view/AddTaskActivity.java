package com.techtoids.nota.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.techtoids.nota.R;
import com.techtoids.nota.adapter.AttachmentAdapter;
import com.techtoids.nota.databinding.ActivityAddTaskBinding;
import com.techtoids.nota.databinding.AttachmentItemBinding;
import com.techtoids.nota.helper.CurrentTaskHelper;

public class AddTaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    ActivityAddTaskBinding binding;
    DatePickerDialog datePickerDialog;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("attachments");
    List<String> attachments = new ArrayList<>();
    AttachmentAdapter adapter;

    Calendar date = Calendar.getInstance();
    boolean isEditing = true;
    Menu menu;
    private String content = "";
    private ActivityResultLauncher<Intent> documentPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Uri uri = result.getData().getData();
            String originalFilename = getFileName(uri);

            String[] fileArr = originalFilename.split("\\.");
            String fileExtension = fileArr[fileArr.length - 1];

            String timeStamp = String.valueOf(new Date().getTime());
            String filename = timeStamp + "." + fileExtension;

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                StorageReference documentRef = storageRef.child(filename);
                UploadTask uploadTask = documentRef.putStream(inputStream);
                startAnimation();
                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return documentRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        attachments.add(downloadUri.toString());
                        adapter.notifyItemInserted(attachments.size() - 1);
                    }
                    stopAnimation();
                }).addOnFailureListener(e -> stopAnimation());
            } catch (FileNotFoundException e) {
                stopAnimation();
                throw new RuntimeException(e);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.taskDescription.disable();
        binding.taskDescription.setHtml("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");

        binding.editDescription.setOnClickListener(v -> {
            System.out.println("called");
        });

        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog
                .getDatePicker()
                .setMinDate(
                        Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())
                                .getTime());

        binding.dueDate.setOnClickListener(v -> {
            showDatePicker();
        });

        binding.editDescription.setOnClickListener(v -> {
            CurrentTaskHelper.instance.content = content;
            Intent intent = new Intent(AddTaskActivity.this, TaskDescriptionActivity.class);
            startActivity(intent);
        });

        binding.addAttachment.setOnClickListener(v -> {
            addDocument();
        });

        adapter = new AttachmentAdapter(attachments, new AttachmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, AttachmentItemBinding binding) {
                String attachment = attachments.get(position);
                openDocument(Uri.parse(attachment));
            }

            @Override
            public void onCancelClick(int position, AttachmentItemBinding binding) {
                attachments.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });
        binding.attachmentList.setAdapter(adapter);
        binding.dueDate.setText(getFormattedDate());
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        content = CurrentTaskHelper.instance.content;
        System.out.println(content);
        binding.taskDescription.setHtml(content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.add_task_menu, menu);
        updateUI();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPress();
                return true;
            case R.id.edit:
                onSaveClick();
                return true;
        }
        return false;
    }

    private String getContent() {
        return binding.taskDescription.getHtml().trim();
    }

    public void updateUI() {
        if (isEditing) {
            if (menu != null)
                menu.findItem(R.id.edit).setTitle("Save");
            binding.taskTitle.setEnabled(true);
            binding.taskTitle.setTextColor(getResources().getColor(R.color.black));
            binding.editDescription.setVisibility(View.VISIBLE);
            binding.addAttachment.setVisibility(View.VISIBLE);
        } else {
            binding.taskTitle.setEnabled(false);
            binding.taskTitle.setTextColor(getResources().getColor(R.color.black));
            binding.editDescription.setVisibility(View.GONE);
            binding.addAttachment.setVisibility(View.GONE);
            if (menu != null)
                menu.findItem(R.id.edit).setTitle("Edit");
        }

        if (attachments.size() == 0 && !isEditing) {
            binding.attachmentHeader.setVisibility(View.GONE);
        } else {
            binding.attachmentHeader.setVisibility(View.VISIBLE);
        }
    }

    private void onSaveClick() {
        if (isEditing) {
            if (binding.taskTitle.getText().toString().trim().length() == 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Oops")
                        .setMessage("Title cannot be empty")
                        .setPositiveButton("Ok", null)
                        .show();
            } else if (binding.taskDescription.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Oops")
                        .setMessage("Description cannot be empty")
                        .setPositiveButton("Ok", null)
                        .show();
            } else if (date.getTime().before(new Date())) {
                new AlertDialog.Builder(this)
                        .setTitle("Oops")
                        .setMessage("Due date cant be past")
                        .setPositiveButton("Ok", null)
                        .show();
            } else {
                isEditing = false;
                CurrentTaskHelper.instance.content = content;
                finish();
            }
        } else {
            isEditing = true;
        }
        updateUI();

        adapter.shouldShowCancel = isEditing;
        adapter.notifyDataSetChanged();
    }

    private void onBackPress() {
        if (isEditing) {
            String content = getContent();
            if (!content.equals(CurrentTaskHelper.instance.content)) {
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
        }
        finish();

    }

    private void showDatePicker() {
        if (!datePickerDialog.isShowing() && isEditing)
            datePickerDialog.show();
    }

    public String getFormattedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy  h:mm a");
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
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void addDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.ms-excel",
                "application/vnd.ms-powerpoint",
                "text/plain"
        });

        documentPickerLauncher.launch(intent);
    }

    public void startAnimation() {
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        binding.progressBarHolder.setAnimation(inAnimation);
        binding.progressBarHolder.setVisibility(View.VISIBLE);
    }

    public void stopAnimation() {
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        binding.progressBarHolder.setAnimation(outAnimation);
        binding.progressBarHolder.setVisibility(View.GONE);
    }

    private void openDocument(Uri mDocumentUri) {
        if (mDocumentUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(mDocumentUri, getMimeTypeFromUri(mDocumentUri));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private String getMimeTypeFromUri(Uri uri) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }
}