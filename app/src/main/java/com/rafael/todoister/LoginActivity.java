package com.rafael.todoister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailEditText;
    private EditText passwordEditText;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        TextView registerTextView = (TextView) findViewById(R.id.go_to_register);
        registerTextView.setOnClickListener(this);

        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.loading_indicator);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_to_register:
                startActivity(new Intent(this, RegistrationActivity.class));
                break;
            case R.id.loginBtn:
                login();
                break;
        }
    }

    private void login(){
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if(email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please provide valid email");
            emailEditText.requestFocus();
            return;
        }

        if(password.length() < 6) {
            passwordEditText.setError("Min password length should be 6 characters!");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        passwordEditText.setEnabled(false);
        emailEditText.setEnabled(false);

        System.out.println(email);
        System.out.println(password);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Toast.makeText(LoginActivity.this, "Failed to login ", Toast.LENGTH_LONG).show();
            }
            progressBar.setVisibility(View.GONE);
            passwordEditText.setEnabled(true);
            emailEditText.setEnabled(true);
        });
    }
}