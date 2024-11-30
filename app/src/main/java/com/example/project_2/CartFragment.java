package com.example.project_2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CartFragment extends Fragment {
    private FirebaseFirestore database;
    private FirebaseUser user;
    private Map<String, Long> cart;
    private List<Product> products;

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

                                        } else {
                                            Log.w("FirestoreData", "Error getting documents.", productsTask.getException());
                                        }

                                        cartListAdapter = new CartListAdapter(products, cart);
                                        recyclerView.setAdapter(cartListAdapter);
                                    });
                        }
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }
                });

        return view;
    }
}