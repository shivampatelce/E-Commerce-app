package com.example.project_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private TextView email;
    private TextView password;
    private Button signInButton;
    private Button goTosignUpButton;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.sign_in_email);
        password = findViewById(R.id.sign_in_password);

        signInButton = findViewById(R.id.sign_in_btn);
        goTosignUpButton = findViewById(R.id.go_to_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        signInButtonClickListener();
        signUpButtonClickListener();
    }

    private void signInButtonClickListener() {
        signInButton.setOnClickListener(view-> {
            if(isValidInput()) {
                signInUser();
            }
        });
    }

    private void signUpButtonClickListener() {
        goTosignUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void signInUser() {
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Intent intent = new Intent(SignInActivity.this, ProductListActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidInput() {
        boolean isValid = true;

        if(email.getText().toString().trim().isEmpty()) {
            email.setError("Email cannot be empty");
            isValid = false;
        }

        if(password.getText().toString().trim().isEmpty()) {
            password.setError("Password cannot be empty");
            isValid = false;
        }

        return isValid;
    }
}