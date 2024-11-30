package com.example.project_2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.MyViewHolder> {
    private List<Product> productList;
    private Map<String, Long> cart;
    private FirebaseUser user;
    private FirebaseFirestore database;

    CartListAdapter(List<Product> productList, Map<String, Long> cart) {
        this.productList = productList;
        this.cart = cart;
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();
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
        Long quantity = cart.get(product.getTitle());

        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("$" + String.valueOf(product.getPrice()));
        holder.quantity.setText(quantity.toString());

        Picasso.get()
                .load(product.getImageURL())
                .into(holder.productImage);

//        Update cart quantity
        holder.updateQntButton.setOnClickListener(view-> {
            updateProductInCart(holder,product, Long.parseLong(holder.quantity.getText().toString()));
        });

//        Remove item from cart
        holder.removeButton.setOnClickListener(view -> {
            removeProduct(holder, product);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle;
        TextView productPrice;
        Button removeButton;
        Button updateQntButton;
        EditText quantity;

        MyViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.cart_card_layout, parent, false));
            productTitle = itemView.findViewById(R.id.cartProductTitle);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            productImage = itemView.findViewById(R.id.cartProductImage);
            updateQntButton = itemView.findViewById(R.id.updateCart);
            removeButton = itemView.findViewById(R.id.removeButton);

            quantity = itemView.findViewById(R.id.quantity);
        }
    }

    private void updateProductInCart(MyViewHolder holder, Product product, Long quantity) {
        String productTitle = product.getTitle();
        if(cart.containsKey(productTitle)) {
            cart.put(productTitle,quantity);
        }

        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(task -> {
                    holder.quantity.clearFocus();
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }

    private void removeProduct(CartListAdapter.MyViewHolder holder, Product product) {
        cart.remove(product.getTitle());
        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(task -> {
                    productList.remove(product);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }
}
