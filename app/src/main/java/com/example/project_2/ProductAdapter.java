package com.example.project_2;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder>{
    private List<Product> productList;
    private FirebaseUser user;
    private FirebaseFirestore database;
    private List<String> cart;

    ProductAdapter(List<Product> productList) {
        this.productList = productList;
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
            updateProductInCart(holder, product);
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
        ImageView productImage;
        CardView productCard;
        Button addToCartButton;

        MyViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.product_card_layout, parent, false));
            productTitle = itemView.findViewById(R.id.productTitle);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            addToCartButton = itemView.findViewById(R.id.addToCart);

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
                        cart = (List<String>) document.get("cart");
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }

    private void setCartButtonText(MyViewHolder holder, Product product) {
        if(cart.contains(product.getTitle())) {
            holder.addToCartButton.setText("Remove From Cart");
            return;
        }
        holder.addToCartButton.setText("Add To Cart");
    }

    private void updateProductInCart(MyViewHolder holder, Product product) {
        String productTitle = product.getTitle();
        if(cart.contains(productTitle)) {
            cart.remove(productTitle);
        } else {
            cart.add(productTitle);
        }

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(aVoid -> {
                    updateCart().addOnCompleteListener(task-> {
                        setCartButtonText(holder, product);
                    });
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));

    }

}
