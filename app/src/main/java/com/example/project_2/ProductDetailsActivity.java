package com.example.project_2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    TextView productTitleTextView;
    TextView productPriceTextView;
    TextView productDescriptionTextView;
    Button addToCartButton;
    ViewPager2 viewPagerImage;
    private RecyclerView.Adapter productImageAdapter;
    private FirebaseFirestore database;
    private FirebaseUser user;
    private List<String> cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addToCartButton = findViewById(R.id.addToCart);
        productTitleTextView = findViewById(R.id.productTitle);
        productPriceTextView = findViewById(R.id.productPrice);
        productDescriptionTextView = findViewById(R.id.productDescription);
        viewPagerImage = findViewById(R.id.viewPagerImage);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();

        String productTitle = getIntent().getStringExtra("productTitle");
        if (productTitle != null) {
            updateCart(productTitle);

            database.collection("Products")
                    .whereEqualTo("title", productTitle)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            List<String> subImages = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String title = document.getString("title");
                                String description = document.getString("description");
                                Double price = document.getDouble("price");
                                String imageURL = document.getString("imageURL");
                                if(document.contains("subImages")) {
                                    subImages = (List<String>) document.get("subImages");

                                    if(subImages.isEmpty()) {
                                        subImages.add(imageURL);
                                    }

                                } else {
                                    subImages.add(imageURL);
                                }

                                productTitleTextView.setText(title);
                                productPriceTextView.setText("$" + String.valueOf(price));
                                productDescriptionTextView.setText(description);

                            }

                            addToCartButton.setOnClickListener(view-> {
                                updateProductInCart(productTitle);
                            });

                            productImageAdapter = new ProductImageAdapter(subImages);
                            viewPagerImage.setAdapter(productImageAdapter);
                        } else {
                            Log.w("FirestoreData", "Error getting documents.", task.getException());
                        }
                    });
        }
    }

    private void updateCart(String productTitle) {
        database.collection("UserDetails")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        cart = (List<String>) document.get("cart");
                        updateCartButtonText(productTitle);
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateProductInCart(String productTitle) {
        if(cart.contains(productTitle)) {
            cart.remove(productTitle);
        } else {
            cart.add(productTitle);
        }

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(aVoid -> {
                    updateCart(productTitle);
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));

    }

    private void updateCartButtonText(String productTitle) {
        if(cart.contains(productTitle)) {
            addToCartButton.setText("Remove From Cart");
        } else {
            addToCartButton.setText("Add To Cart");
        }
    }

}