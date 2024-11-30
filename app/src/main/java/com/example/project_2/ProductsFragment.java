package com.example.project_2;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter productAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Product> productList;
    private FirebaseFirestore database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        recyclerView = view.findViewById(R.id.product_list_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        database = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        database.collection("Products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String subDescription = document.getString("subDescription");
                            Double price = document.getDouble("price");
                            String imageURL = document.getString("imageURL");
                            List<String> subImages = (List<String>) document.get("subImages");
                            productList.add(new Product(title,description,subDescription, price,imageURL, subImages));
                        }
                    } else {
                        Log.w("FirestoreData", "Error getting documents.", task.getException());
                    }

                    productAdapter.notifyDataSetChanged();
                });


        return view;
    }
}