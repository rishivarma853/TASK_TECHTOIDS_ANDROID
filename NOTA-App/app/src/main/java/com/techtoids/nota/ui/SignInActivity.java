package com.techtoids.nota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

import com.techtoids.nota.R;
import com.techtoids.nota.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "Persistence";
    ActivitySignInBinding binding;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(), (result) -> {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            navigateToHome(true);
        } else {
            if (response == null) {
                showSnackbar("Sign in cancelled");
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK || response.getError().getErrorCode() == ErrorCodes.PROVIDER_ERROR) {
                showSnackbar("Network error, Check your internet connection.");
                return;
            }

            showSnackbar("Unknown error");
            Log.d(TAG, "Sign-in error: " + response.getError().getErrorCode());
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        if (isSignedIn()) {
            navigateToHome(false);
        } else {
            startSignIn();
        }
    }

    private boolean isSignedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    private void startSignIn() {
        Intent signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.Theme_NOTAApp).setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(), new AuthUI.IdpConfig.EmailBuilder().build())).setLogo(R.mipmap.ic_launcher).build();
        signInLauncher.launch(signInIntent);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void navigateToHome(boolean showSnackbar) {
        Intent intent = new Intent(SignInActivity.this, HomeScreenActivity.class);
        intent.putExtra("showSnackbar", showSnackbar);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}