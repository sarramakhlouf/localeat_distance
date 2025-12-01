package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.data.FirebaseAuthService;
import com.iset.dsi.localeat.utils.Validator;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etVerifyPassword;
    private Button btnSignup;
    private TextView tvGoLogin;

    private ImageView backBtn,ivTogglePassword,ivToggleVerifyPassword, profileImage;
    private Uri selectedImageUri = null;

    private FirebaseAuthService authService;

    // Picker d’image
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(profileImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authService = new FirebaseAuthService();

        etUsername = findViewById(R.id.inputUsername);
        etEmail = findViewById(R.id.inputEmail);
        etPassword = findViewById(R.id.inputPassword);
        etVerifyPassword = findViewById(R.id.inputVerifyPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvGoLogin = findViewById(R.id.tvGoSignup);
        backBtn = findViewById(R.id.backBtn);
        profileImage = findViewById(R.id.profileImage);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleVerifyPassword = findViewById(R.id.ivToggleVerifyPassword);
        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        ivToggleVerifyPassword.setOnClickListener(v -> {
            if (etVerifyPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etVerifyPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etVerifyPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etVerifyPassword.setSelection(etVerifyPassword.getText().length());
        });

        // Ouvrir picker d’image
        profileImage.setOnClickListener(v -> imagePicker.launch("image/*"));

        // Aller au Login
        tvGoLogin.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));

        // Bouton Signup
        btnSignup.setOnClickListener(v -> signupUser());

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, SplashActivity.class)));
    }

    private void signupUser() {
        String name = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String verifyPwd = etVerifyPassword.getText().toString().trim();

        if (!Validator.isFieldNotEmpty(name)) { etUsername.setError("Nom requis"); return; }
        if (!Validator.isEmailValid(email)) { etEmail.setError("Email invalide"); return; }
        if (!Validator.isPasswordValid(password)) { etPassword.setError("Min 6 caractères"); return; }
        if (!password.equals(verifyPwd)) { etVerifyPassword.setError("Ne correspond pas"); return; }

        authService.signup(this, email, password, name, null, selectedImageUri,
                success -> {
                    Toast.makeText(SignupActivity.this, "Compte créé", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(SignupActivity.this, error.getMessage(), Toast.LENGTH_LONG).show()
        );
    }
}
