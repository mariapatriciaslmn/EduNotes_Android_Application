package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passwordInput;
    Button registerBtn;
    TextView goSignIn;
    OnlineDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHandler = new OnlineDBHandler(this);

        nameInput = findViewById(R.id.crtName);
        emailInput = findViewById(R.id.rgEmail);
        passwordInput = findViewById(R.id.crtPassword);
        registerBtn = findViewById(R.id.btnRegister);
        goSignIn = findViewById(R.id.goSignIn);

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            } else if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            } else {
                dbHandler.signupUser(name, email, password, new OnlineDBHandler.SignupListener() {
                    @Override
                    public void onSignupSuccess() {
                        Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onSignupFailed(String errorMessage) {
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        goSignIn.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }
}