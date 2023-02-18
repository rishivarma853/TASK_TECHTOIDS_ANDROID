package ca.lcit22fw.madt.techtoids.android.nota_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.lcit22fw.madt.techtoids.android.nota_app.databinding.ActivityTaskScreenBinding;

public class TaskScreenActivity extends AppCompatActivity {

    ActivityTaskScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskScreenBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());

        if (!isSignedIn()) {
            startSignIn();
            return;
        }

        binding.home.setOnClickListener(v -> {finish();});
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSignedIn()) {
            Intent intent = getIntent();
            boolean showSnackbar = intent.getBooleanExtra("showSnackbar", false);
            if (showSnackbar) {
                FirebaseUser user = getUser();
                if (user != null) {
                    String name = user.getDisplayName();
                    Snackbar.make(binding.getRoot(), "Welcome " + name, Snackbar.LENGTH_SHORT).show();
                }
            }
        } else {
            startSignIn();
        }
    }

    private FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private boolean isSignedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    private void startSignIn() {
        Intent intent = new Intent(TaskScreenActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}