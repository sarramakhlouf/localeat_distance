package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.data.FirebaseAuthService;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnRecoverPassword;

    private ImageView backBtn;
    private FirebaseAuthService authService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password); // ton XML

        inputEmail = findViewById(R.id.inputEmail);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);
        backBtn = findViewById(R.id.backBtn);
        authService = new FirebaseAuthService();

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRecoverPassword.setOnClickListener(v -> {

            String email = inputEmail.getText().toString().trim();
            if(email.isEmpty()){
                Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Appel Firebase pour envoyer le mail de réinitialisation
            authService.recoverPassword(email,
                    aVoid -> Toast.makeText(this, "Email envoyé ! Vérifiez votre boîte mail", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }
}
