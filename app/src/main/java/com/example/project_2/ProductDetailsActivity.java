package com.example.project_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    TextView productTitleTextView;
    TextView productPriceTextView;
    TextView productDescriptionTextView;
    TextView quantity;
    ImageButton addToCartButton, backButton;
    Button incrementButton;
    Button decrementButton;

    ViewPager2 viewPagerImage;
    private RecyclerView.Adapter productImageAdapter;
    private FirebaseFirestore database;
    private FirebaseUser user;
    private Map<String, Long> cart;

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
        quantity = findViewById(R.id.quantity);
        incrementButton = findViewById(R.id.increaseQnt);
        decrementButton = findViewById(R.id.decreaseQnt);
        backButton = findViewById(R.id.backButton);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainLayoutActivity.class);
            startActivity(intent);
        });

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
                                updateProductInCart(productTitle, Long.parseLong(quantity.getText().toString()));
                            });

                            incrementButton.setOnClickListener(view -> {
                                long cartQuantity = Long.parseLong(quantity.getText().toString()) + 1;
                                if(cartQuantity <= 5) {
                                    quantity.setText(String.valueOf(cartQuantity));
                                    updateProductQuantityInCart(productTitle, cartQuantity);
                                }
                            });

                            decrementButton.setOnClickListener(view -> {
                                long cartQuantity = Long.parseLong(quantity.getText().toString()) - 1;
                                if(cartQuantity > 0) {
                                    quantity.setText(String.valueOf(cartQuantity));
                                    updateProductQuantityInCart(productTitle, cartQuantity);
                                }
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
                        cart = (Map<String, Long>) document.get("cart");
                        updateCartButtonText(productTitle);
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateProductInCart(String productTitle, Long quantity) {
        if(cart.containsKey(productTitle)) {
            cart.remove(productTitle);
            this.quantity.setText("1");
        } else {
            cart.put(productTitle, quantity);
        }

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(aVoid -> {
                    updateCart(productTitle);
                    this.quantity.clearFocus();
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }

    private void updateProductQuantityInCart(String productTitle, Long quantity) {
        cart.put(productTitle, quantity);

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(aVoid -> {
                    updateCart(productTitle);
                    this.quantity.clearFocus();
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }

    private void updateCartButtonText(String productTitle) {
        if(cart.containsKey(productTitle)) {
            quantity.setText(cart.get(productTitle).toString());
            addToCartButton.setImageResource(R.drawable.baseline_remove_shopping_cart_24);
        } else {
            addToCartButton.setImageResource(R.drawable.baseline_shopping_cart_24);
        }
    }

}