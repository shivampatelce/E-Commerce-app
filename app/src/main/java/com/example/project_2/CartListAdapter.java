package com.example.project_2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private OnDataChangeListener dataChangeListener;

    CartListAdapter(List<Product> productList, Map<String, Long> cart, OnDataChangeListener listener) {
        this.productList = productList;
        this.cart = cart;
        this.dataChangeListener = listener;
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();
    }

    interface OnDataChangeListener {
        void onDataChanged();
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
        holder.productPrice.setText("$" + String.format("%.2f",product.getPrice()));
        if(quantity != null) {
            holder.quantity.setText(quantity.toString());
        }

        Picasso.get()
                .load(product.getImageURL())
                .into(holder.productImage);

//        Increase cart quantity
        holder.incrementButton.setOnClickListener(view-> {
            long cartQuantity = Long.parseLong(holder.quantity.getText().toString()) + 1;
            if(cartQuantity <= 5) {
                holder.quantity.setText(String.valueOf(cartQuantity));
                updateProductInCart(holder, product, cartQuantity);
            }
        });

//        Decrease cart quantity
        holder.decrementButton.setOnClickListener((view)-> {
            long cartQuantity = Long.parseLong(holder.quantity.getText().toString()) - 1;
            if(cartQuantity > 0) {
                holder.quantity.setText(String.valueOf(cartQuantity));
                updateProductInCart(holder, product, cartQuantity);
            }
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
        ImageButton removeButton;
        EditText quantity;
        Button incrementButton;
        Button decrementButton;

        MyViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.cart_card_layout, parent, false));
            productTitle = itemView.findViewById(R.id.cartProductTitle);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            productImage = itemView.findViewById(R.id.cartProductImage);
            removeButton = itemView.findViewById(R.id.removeButton);
            incrementButton = itemView.findViewById(R.id.increaseQnt);
            decrementButton = itemView.findViewById(R.id.decreaseQnt);

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
                    if (dataChangeListener != null) {
                        dataChangeListener.onDataChanged();
                    }
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }

    private void removeProduct(CartListAdapter.MyViewHolder holder, Product product) {
        cart.remove(product.getTitle());
        database.collection("UserDetails").document(user.getUid())
                .update("cart", cart)
                .addOnSuccessListener(task -> {
                    productList.remove(product);
                    if (dataChangeListener != null) {
                        dataChangeListener.onDataChanged();
                    }
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.w("FirestoreData", "Error updating document", e));
    }
}
