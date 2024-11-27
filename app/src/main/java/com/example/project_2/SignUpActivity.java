package com.example.project_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText password;
    private EditText confirmPassword;
    private Button signUpButton;
    private Button goToSignInButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        emailEditText = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.sign_up);
        goToSignInButton = findViewById(R.id.go_to_sign_in);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        handleSignUpButtonClick();
        handleGoToSignInButtonClick();
    }

    private void handleSignUpButtonClick() {
        signUpButton.setOnClickListener(view -> {
            if(isValidInput()) {
                createUser();
            }
        });
    }

    private void createUser() {
        firebaseAuth.createUserWithEmailAndPassword(emailEditText.getText().toString().trim(), password.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            database = FirebaseFirestore.getInstance();
                            // Get the user ID (uid)
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            // Create a map of user details
                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("firsName", firstNameEditText.getText().toString().trim());
                            userDetails.put("lastName", lastNameEditText.getText().toString().trim());
                            userDetails.put("cart", new ArrayList<String>());

                            // Save user details in Firestore
                            database.collection("UserDetails").document(uid)
                                    .set(userDetails)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.i("UserCreate", "User is created.");
                                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> Log.w("FirestoreData", "Error adding user details", e));
                        } else {
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidInput() {
        boolean isValid = true;

        if(firstNameEditText.getText().toString().trim().isEmpty()) {
            firstNameEditText.setError("First name cannot be empty");
            isValid = false;
        }

        if(lastNameEditText.getText().toString().trim().isEmpty()) {
            lastNameEditText.setError("Last name cannot be empty");
            isValid = false;
        }

        if(emailEditText.getText().toString().trim().isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            isValid = false;
        }

        String password = this.password.getText().toString().trim();
        String confirmPassword = this.confirmPassword.getText().toString().trim();

        if(password.isEmpty()) {
            this.password.setError("Password cannot be empty");
            isValid = false;
        } else if(password.length() < 6) {
            this.password.setError("At least 6 characters are required.");
            isValid = false;
        }

        if(confirmPassword.isEmpty()) {
            this.confirmPassword.setError("Confirm password cannot be empty");
            isValid = false;
        } else if(!password.equals(confirmPassword)) {
            this.confirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void handleGoToSignInButtonClick() {
        goToSignInButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        });
    }
}