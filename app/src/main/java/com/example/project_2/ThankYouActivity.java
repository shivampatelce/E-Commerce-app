package com.example.project_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ThankYouActivity extends AppCompatActivity {

    private Button shopMoreButton;
    private FirebaseFirestore database;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thank_you);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        shopMoreButton = findViewById(R.id.shopMoreButton);

        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        database.collection("UserDetails")
                .document(user.getUid())
                .update("cart", new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    Log.i("Firestore", "Cart items removed.");
                })
                .addOnFailureListener(e -> {
                    // Error occurred while removing the field
                    Log.w("Firestore", "Error removing cart field", e);
                });

        shopMoreButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainLayoutActivity.class);
            startActivity(intent);
        });
    }
}