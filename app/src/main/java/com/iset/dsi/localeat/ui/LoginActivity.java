package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.iset.dsi.localeat.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.data.FirebaseAuthService;
import com.iset.dsi.localeat.utils.Validator;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmailOrUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoSignup, tvForgotPassword;
    private ImageView ivTogglePassword, backBtn;

    private FirebaseAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // ton XML

        authService = new FirebaseAuthService();

        etEmailOrUsername = findViewById(R.id.inputEmailOrUsername);
        etPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        backBtn = findViewById(R.id.backBtn);
        tvGoSignup = findViewById(R.id.tvGoSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        // Toggle password visibility
        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Navigate to Signup
        tvGoSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        // Login Button
        btnLogin.setOnClickListener(v -> loginUser());

        // Navigate to Forget Password
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

    }

    private void loginUser() {
        String emailOrUsername = etEmailOrUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (emailOrUsername.isEmpty()) {
            etEmailOrUsername.setError("Veuillez entrer un email ou un nom d'utilisateur");
            return;
        }

        if (!Validator.isPasswordValid(password)) {
            etPassword.setError("6 caractères minimum");
            return;
        }

        authService.loginWithEmailOrUsername(this, emailOrUsername, password,
                user -> {
                    Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("openFragment", "FragmentHome"); // Indiquer qu'on veut ouvrir FragmentHome
                    startActivity(intent);
                    finish();
                },
                e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

}

