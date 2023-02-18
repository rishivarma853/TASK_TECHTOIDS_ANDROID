package com.techtoids.nota.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.techtoids.nota.databinding.ActivityTaskScreenBinding;

public class TaskScreenActivity extends AppCompatActivity {

    ActivityTaskScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskScreenBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());

        binding.home.setOnClickListener(v -> {
            finish();
        });


    }

}