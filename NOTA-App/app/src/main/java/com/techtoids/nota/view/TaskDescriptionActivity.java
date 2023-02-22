package com.techtoids.nota.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtoids.nota.R;
import com.techtoids.nota.adapter.FormatOptionsAdapter;
import com.techtoids.nota.databinding.ActivityTaskDescriptionBinding;
import com.techtoids.nota.helper.CurrentTaskHelper;
import com.techtoids.nota.model.FormatOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDescriptionActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    ActivityTaskDescriptionBinding binding;
    List<FormatOption> formatOptionList = new ArrayList<>();
    FormatOptionsAdapter adapter;
    Menu menu;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");
    private ActivityResultLauncher<Intent> imagePickerResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                Bitmap selectedImageBitmap;
                try {
                    selectedImageBitmap = rectifyImage(selectedImageUri);
                    Uri filepath = saveImage(selectedImageBitmap);
                    if (filepath != null) {
                        uploadImage(filepath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDescriptionBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.taskDescription.enableFullscreen();
        String description = CurrentTaskHelper.instance.taskData.getDescription();
        if (description != null) {
            binding.taskHeaderLayout.header.setText("Update Description");
            binding.taskDescription.setHtml(description);
        } else {
            binding.taskHeaderLayout.header.setText("Add Description");
        }
        binding.taskDescription.requestFocus();

        setupData();
        adapter = new FormatOptionsAdapter(formatOptionList);
        binding.formatToolbar.setAdapter(adapter);

        binding.taskHeaderLayout.home.setOnClickListener(v -> onBackPress());
        binding.taskHeaderLayout.save.setOnClickListener(v -> onSaveClick());
    }

    private void setupData() {
        formatOptionList.add(new FormatOption(R.drawable.undo, () -> binding.taskDescription.undo()));
        formatOptionList.add(new FormatOption(R.drawable.redo, () -> binding.taskDescription.redo()));
        formatOptionList.add(new FormatOption(R.drawable.format_clear, () -> binding.taskDescription.removeFormat()));
        formatOptionList.add(new FormatOption(R.drawable.format_size, () -> {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(TaskDescriptionActivity.this);
            alertDialog.setTitle("Select Heading");

            final String[] headingArray = {"Heading 1", "Heading 2", "Heading 3", "Heading 4", "Heading 5", "Heading 6"};


            final int checkedItem = 4;
            alertDialog.setSingleChoiceItems(headingArray, checkedItem, (dialog, which) -> {
                binding.taskDescription.setHeading(which + 1);
                dialog.dismiss();
            });

            AlertDialog alert = alertDialog.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }));
        formatOptionList.add(new FormatOption(R.drawable.format_color_text, () -> {
            ColorPickerDialogBuilder.with(TaskDescriptionActivity.this).initialColor(Color.WHITE).setTitle("Choose color").wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12).setOnColorSelectedListener(selectedColor -> binding.taskDescription.setTextColor(selectedColor)).setPositiveButton("Ok", (dialog, selectedColor, allColors) -> binding.taskDescription.setTextColor(selectedColor)).setNegativeButton("Cancel", (dialog, which) -> {
            }).build().show();
        }));
        formatOptionList.add(new FormatOption(R.drawable.format_bold, () -> binding.taskDescription.setBold()));
        formatOptionList.add(new FormatOption(R.drawable.format_italic, () -> binding.taskDescription.setItalic()));
        if (checkCameraHardware())
            formatOptionList.add(new FormatOption(R.drawable.camera, () -> requestCameraPermissions()));
        formatOptionList.add(new FormatOption(R.drawable.image, () -> onAddImagePress()));
        formatOptionList.add(new FormatOption(R.drawable.format_align_left, () -> binding.taskDescription.setAlignLeft()));
        formatOptionList.add(new FormatOption(R.drawable.format_align_center, () -> binding.taskDescription.setAlignCenter()));
        formatOptionList.add(new FormatOption(R.drawable.format_align_right, () -> binding.taskDescription.setAlignRight()));
        formatOptionList.add(new FormatOption(R.drawable.format_list_bulleted, () -> binding.taskDescription.setUnorderedList()));
        formatOptionList.add(new FormatOption(R.drawable.format_list_numbered, () -> binding.taskDescription.setOrderedList()));
        formatOptionList.add(new FormatOption(R.drawable.format_indent_increase, () -> binding.taskDescription.setIndent()));
        formatOptionList.add(new FormatOption(R.drawable.format_indent_decrease, () -> binding.taskDescription.setOutdent()));
    }

    private void onAddImagePress() {
        binding.taskDescription.clearFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imagePickerResult.launch(new Intent(MediaStore.ACTION_PICK_IMAGES).setType("image/*"));
        } else {
            imagePickerResult.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"));
        }
    }

    private void uploadImage(Uri file) {
        StorageReference imageRef = storageRef.child(file.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(file);
        startAnimation();
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                binding.taskDescription.insertImage(downloadUri.toString());
            }
            stopAnimation();
        }).addOnFailureListener(e -> stopAnimation());
    }

    private Uri saveImage(Bitmap finalBitmap) {
        String root = getFilesDir().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        String timeStamp = String.valueOf(new Date().getTime());
        String filename = timeStamp + ".jpg";

        File file = new File(myDir, filename);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap rectifyImage(Uri uri) throws IOException {
        Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            ExifInterface ei = new ExifInterface(input);

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(originalBitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(originalBitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(originalBitmap, 270);
                default:
                    return originalBitmap;
            }
        } catch (Exception e) {
            return originalBitmap;
        }
    }

    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    public void onBackPressed() {
        onBackPress();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.task_description_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPress();
                return true;
            case R.id.save:
                onSaveClick();
                return true;
        }
        return false;
    }

    private String getContent() {
        return binding.taskDescription.getHtml().trim();
    }

    private void onSaveClick() {
        String content = getContent();
        if (binding.taskDescription.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("No changes Detected")
                    .setMessage("There is nothing to save. Add something to save")
                    .setPositiveButton("Ok", null)
                    .show();
        } else {
            CurrentTaskHelper.instance.taskData.setDescription(content);
            finish();
        }
    }

    private void onBackPress() {
        String content = getContent();
        if (!content.equals(CurrentTaskHelper.instance.taskData.getDescription())) {
            new AlertDialog.Builder(this)
                    .setTitle("Changes Detected")
                    .setMessage("Do you want to discard changes and go back?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        System.out.println("yes");
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            finish();
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

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                System.out.println(data.getExtras().get("data"));
                System.out.println(data.getData());
                if (data.getExtras() != null) {
                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    Uri filepath = saveImage(thumbnail);
                    System.out.println(filepath);
                    if (filepath != null) {
                        uploadImage(filepath);
                    }
                }
            }
        }
    });

    void onPermissionGranted() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }
}