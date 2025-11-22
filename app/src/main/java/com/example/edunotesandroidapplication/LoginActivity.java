package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView goSignUp;
    OnlineDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHandler = new OnlineDBHandler(this);

        emailInput = findViewById(R.id.enEmail);
        passwordInput = findViewById(R.id.enPassword);
        loginBtn = findViewById(R.id.LoginBtn);
        goSignUp = findViewById(R.id.goSignUp);

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHandler.checkUserLogin(email, password, new OnlineDBHandler.LoginListener() {
                @Override
                public void onLoginSuccess() {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onLoginFailed(String errorMessage) {
                    Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        goSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }
}