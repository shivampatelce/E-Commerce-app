package com.example.project_2;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartFragment extends Fragment implements CartListAdapter.OnDataChangeListener {
    private FirebaseFirestore database;
    private FirebaseUser user;
    private Map<String, Long> cart;
    private List<Product> products;
    private Button checkoutButton;
    private TextView subtotalAmount;
    private TextView taxAmount;
    private TextView totalAmount;
    private LinearLayout billLayout;
    private ConstraintLayout emptyCartMessageLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter cartListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        products = new ArrayList<>();

        recyclerView = view.findViewById(R.id.cart_list_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        checkoutButton = view.findViewById(R.id.checkout);
        checkoutButton.setVisibility(View.GONE);

        subtotalAmount = view.findViewById(R.id.subtotalAmount);
        taxAmount = view.findViewById(R.id.taxAmount);
        totalAmount = view.findViewById(R.id.totalAmount);
        billLayout = view.findViewById(R.id.bill);
        emptyCartMessageLayout = view.findViewById(R.id.emptyCart);
        emptyCartMessageLayout.setVisibility(View.GONE);

        setCartList();

        return view;
    }

    private void setCartList() {
        products = new ArrayList<>();
        cart = new HashMap<>();
        database.collection("UserDetails")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        cart = (Map<String, Long>) document.get("cart");

                        ArrayList<String> cartProducts = new ArrayList<>();
                        cart.keySet().forEach(cartItem -> {
                            cartProducts.add(cartItem);
                        });

                        if(!cartProducts.isEmpty()) {
                            database.collection("Products")
                                    .whereIn(FieldPath.documentId(), cartProducts)
                                    .get()
                                    .addOnCompleteListener(productsTask -> {

                                        if (productsTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot productDocument : productsTask.getResult()) {
                                                String title = productDocument.getString("title");
                                                String description = productDocument.getString("description");
                                                String subDescription = productDocument.getString("subDescription");
                                                String imageURL = productDocument.getString("imageURL");
                                                Double price = productDocument.getDouble("price");
                                                List<String> subImages = (List<String>) productDocument.get("subImages");

                                                products.add(new Product(title, description, subDescription, price, imageURL, subImages));
                                            }

                                            if(products.isEmpty()) {
                                                checkoutButton.setVisibility(View.GONE);
                                            } else {
                                                checkoutButton.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            Log.w("FirestoreData", "Error getting documents.", productsTask.getException());
                                            checkoutButton.setVisibility(View.GONE);
                                        }

                                        cartListAdapter = new CartListAdapter(products, cart, this);
                                        calculateBillAmount();
                                        recyclerView.setAdapter(cartListAdapter);
                                    });
                        } else {
                            calculateBillAmount();
                        }
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });
    }

    private void calculateBillAmount() {
        if(this.products.isEmpty()) {
            this.subtotalAmount.setText("$0.00");
            this.taxAmount.setText("$0.00");
            this.totalAmount.setText("$0.00");

            checkoutButton.setVisibility(View.GONE);
            billLayout.setVisibility(View.GONE);
            emptyCartMessageLayout.setVisibility(View.VISIBLE);
            return;
        }

        checkoutButton.setVisibility(View.VISIBLE);
        billLayout.setVisibility(View.VISIBLE);
        emptyCartMessageLayout.setVisibility(View.GONE);

        double subtotalAmount = 0d;
        double taxAmount = 0d;
        double totalAmount = 0d;
        for (Product product : products) {
            subtotalAmount += product.getPrice() * cart.get(product.getTitle());
        }
        this.subtotalAmount.setText("$" + String.valueOf(subtotalAmount));

        taxAmount = subtotalAmount * 0.13;
        this.taxAmount.setText("$" + String.valueOf(taxAmount));

        totalAmount = subtotalAmount + taxAmount;
        this.totalAmount.setText("$" + String.valueOf(totalAmount));
    }

    @Override
    public void onDataChanged() {
        setCartList();
    }
}