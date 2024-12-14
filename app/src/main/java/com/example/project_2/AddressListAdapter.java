package com.example.project_2;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.MyViewHolder> {
    private List<Address> addressList;
    private FirebaseFirestore database;
    private FirebaseUser user;
    private AddressListAdapter.OnDataChangeListener dataChangeListener;

    AddressListAdapter(List<Address> addressList, AddressListAdapter.OnDataChangeListener listener) {
        this.addressList = addressList;
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        dataChangeListener = listener;
    }

    interface OnDataChangeListener {
        void onDataChanged();
    }

    @NonNull
    @Override
    public AddressListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new AddressListAdapter.MyViewHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.addressLine.setText(address.getAddressLine());
        holder.city.setText(address.getCity());
        holder.province.setText(address.getProvince());
        holder.postalCode.setText(address.getPostalCode());
        holder.country.setText(address.getCountry());

        holder.selectButton.setOnClickListener(view-> {
            Intent intent = new Intent(view.getContext(), PaymentActivity.class);
            view.getContext().startActivity(intent);
        });

        holder.removeButton.setOnClickListener(view -> {
            DocumentReference docRef = database.collection("UserDetails").document(user.getUid());

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> addresses = (List<String>) documentSnapshot.get("addresses");

                    if (addresses != null && !addresses.isEmpty()) {
                        addresses.remove(position);

                        docRef.update("addresses", addresses)
                                .addOnSuccessListener(aVoid -> {
                                    if (dataChangeListener != null) {
                                        dataChangeListener.onDataChanged();
                                    }
                                    Log.d("Firestore", "Array field updated successfully!");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error updating array field", e);
                                });
                    } else {
                        Log.e("Firestore", "Array field is null or empty");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Error retrieving document", e);
            });
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView addressLine;
        TextView city;
        TextView province;
        TextView postalCode;
        TextView country;
        Button selectButton, removeButton;

        MyViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.address_layout, parent, false));

            addressLine = itemView.findViewById(R.id.addressLine);
            city = itemView.findViewById(R.id.city);
            province = itemView.findViewById(R.id.province);
            postalCode = itemView.findViewById(R.id.postalCode);
            country = itemView.findViewById(R.id.country);
            selectButton = itemView.findViewById(R.id.selectButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
