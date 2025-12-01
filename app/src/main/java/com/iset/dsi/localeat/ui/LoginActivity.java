package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        setContentView(R.layout.activity_login);

        authService = new FirebaseAuthService();

        etEmailOrUsername = findViewById(R.id.inputEmailOrUsername);
        etPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        backBtn = findViewById(R.id.backBtn);
        tvGoSignup = findViewById(R.id.tvGoSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword));

        tvGoSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));
        backBtn.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void togglePasswordVisibility(EditText et) {
        if (et.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        et.setSelection(et.getText().length());
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
                    intent.putExtra("openFragment", "FragmentHome");
                    startActivity(intent);
                    finish();
                },
                e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }
}