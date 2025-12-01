package com.iset.dsi.localeat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iset.dsi.localeat.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // 🔹 Auto-login Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("openFragment", "FragmentHome");
            startActivity(intent);
            finish();
            return;
        }

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(SplashActivity.this, LoginActivity.class)));
    }
}