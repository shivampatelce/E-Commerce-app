package com.example.project_2;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder>{
    private List<Product> productList;
    private FirebaseUser user;
    private FirebaseFirestore database;
    private Map<String, Long> cart;

    ProductAdapter(List<Product> productList) {
        this.productList = productList;
        cart = new HashMap<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();
        updateCart();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new MyViewHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productDescription.setText(product.getSubDescription());
        holder.productPrice.setText("$"+String.valueOf(product.getPrice()));

        setCartButtonText(holder, product);

//        Set image
        Picasso.get()
                .load(product.getImageURL())
                .into(holder.productImage);

        holder.productCard.setOnClickListener(view-> {
            Intent intent = new Intent(view.getContext(), ProductDetailsActivity.class);
            intent.putExtra("productTitle", product.getTitle());
            view.getContext().startActivity(intent);
        });

        holder.addToCartButton.setOnClickListener(view-> {
            long quantity = Long.parseLong(holder.quantity.getText().toString());
            if (quantity == 0) {
                quantity = 1L;
            }
            removeProductInCart(holder, product, quantity);
        });

        holder.incrementButton.setOnClickListener(view -> {
            long quantity = Long.parseLong(holder.quantity.getText().toString()) + 1;
            if(quantity <= 10) {
                holder.quantity.setText(String.valueOf(quantity));
                updateProductInCart(holder, product, quantity);
            }
        });

        holder.decreaseButton.setOnClickListener(view -> {
            long quantity = Long.parseLong(holder.quantity.getText().toString()) - 1;
            if(quantity > 0) {
                holder.quantity.setText(String.valueOf(quantity));
                updateProductInCart(holder, product, quantity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView productTitle;
        TextView productDescription;
        TextView productPrice;
        EditText quantity;
        ImageView productImage;
        CardView productCard;
        ImageButton addToCartButton;

        Button incrementButton;
        Button decreaseButton;

        MyViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.product_card_layout, parent, false));
            productTitle = itemView.findViewById(R.id.productTitle);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            addToCartButton = itemView.findViewById(R.id.addToCart);
            quantity = itemView.findViewById(R.id.quantity);

            incrementButton = itemView.findViewById(R.id.increaseQnt);
            decreaseButton = itemView.findViewById(R.id.decreaseQnt);

            productCard = itemView.findViewById(R.id.productCard);
        }
    }

    private Task<DocumentSnapshot> updateCart() {
        return database.collection("UserDetails")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        cart = (Map<String, Long>) document.get("cart");
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }

    private void setCartButtonText(MyViewHolder holder, Product product) {
        if(cart.containsKey(product.getTitle())) {
            holder.quantity.setText(cart.get(product.getTitle()).toString());
            holder.addToCartButton.setImageResource(R.drawable.baseline_remove_shopping_cart_24);
            return;
        }
        holder.addToCartButton.setImageResource(R.drawable.baseline_shopping_cart_24);
    }

    private void removeProductInCart(MyViewHolder holder, Product product, Long quantity) {
        String productTitle = product.getTitle();
        if(cart.containsKey(productTitle)) {
            cart.remove(productTitle);
            holder.quantity.setText("1");
        } else {
            cart.put(productTitle, quantity);
        }

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(aVoid -> {
                    updateCart().addOnCompleteListener(task-> {
                        setCartButtonText(holder, product);
                        holder.quantity.clearFocus();
                    });
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }

    private void updateProductInCart(MyViewHolder holder, Product product, Long quantity) {
        String productTitle = product.getTitle();
        cart.put(productTitle, quantity);

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(aVoid -> {
                    updateCart().addOnCompleteListener(task-> {
                        setCartButtonText(holder, product);
                        holder.quantity.clearFocus();
                    });
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));

    }

}
