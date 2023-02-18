package ca.lcit22fw.madt.techtoids.android.nota_app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.lcit22fw.madt.techtoids.android.nota_app.R;
import ca.lcit22fw.madt.techtoids.android.nota_app.adapter.FormatOptionsAdapter;
import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.ActivityTaskDescriptionBinding;
import ca.lcit22fw.madt.techtoids.android.nota_app.helper.CurrentTaskHelper;
import ca.lcit22fw.madt.techtoids.android.nota_app.model.FormatOption;

public class TaskDescriptionActivity extends AppCompatActivity {

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
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
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

        binding.taskDescription.setHtml(CurrentTaskHelper.instance.content);
        binding.taskDescription.requestFocus();

        setupData();
        adapter = new FormatOptionsAdapter(formatOptionList);
        binding.formatToolbar.setAdapter(adapter);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupData() {
        formatOptionList.add(new FormatOption(ca.lcit22fw.madt.techtoids.android.nota_app.R.drawable.undo, () -> binding.taskDescription.undo()));
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
            CurrentTaskHelper.instance.content = content;
            finish();
        }
    }

    private void onBackPress() {
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
        } else {
            finish();
        }
    }

    public void startAnimation() {
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        binding.progressBarHolder.setAnimation(inAnimation);
        binding.progressBarHolder.setVisibility(View.VISIBLE);
        menu.findItem(R.id.save).setVisible(false);
    }

    public void stopAnimation() {
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        binding.progressBarHolder.setAnimation(outAnimation);
        binding.progressBarHolder.setVisibility(View.GONE);
        menu.findItem(R.id.save).setVisible(true);
    }
}