package com.example.project_2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddAddressActivity extends AppCompatActivity implements AddressListAdapter.OnDataChangeListener {

    private Toolbar topToolbar;
    private EditText addressLine;
    private EditText city;
    private EditText province;
    private EditText postalCode;
    private EditText country;
    private Button addAddressButton;
    private FirebaseFirestore database;
    private FirebaseUser user;
    private LinearLayout addressViewLayout;
    private double amount;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter addressListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Address> addressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        amount = getIntent().getDoubleExtra("totalPaymentAmount", 0d);

        recyclerView = findViewById(R.id.addressListRecyclerView);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        addressList = new ArrayList<>();
        addressListAdapter = new AddressListAdapter(addressList, amount, this);
        recyclerView.setAdapter(addressListAdapter);

        topToolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(topToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Checkout");
        }

        addressLine = findViewById(R.id.addressLine);
        city = findViewById(R.id.city);
        province = findViewById(R.id.province);
        postalCode = findViewById(R.id.postalCode);
        country = findViewById(R.id.country);
        addAddressButton = findViewById(R.id.addAddressBtn);
        addressViewLayout = findViewById(R.id.addressView);

        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        addAddressButton.setOnClickListener(v -> {
            if(isAddAddressFormValid()) {
                addNewAddress();
            }
        });

        setAddressList();
    }

    private void setAddressList() {
        database.collection("UserDetails")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        addressList.clear();
                        List<Map<String, Object>> fetchedAddresses = (List<Map<String, Object>>) documentSnapshot.get("addresses");
                        if (fetchedAddresses != null) {
                            addressList.clear();
                            for (Map<String, Object> map : fetchedAddresses) {
                                Address address = new Address(
                                        (String) map.get("addressLine"),
                                        (String) map.get("city"),
                                        (String) map.get("province"),
                                        (String) map.get("postalCode"),
                                        (String) map.get("country")
                                );
                                addressList.add(address);
                                addressListAdapter.notifyDataSetChanged();
                            }
                        }

                        if(fetchedAddresses.isEmpty()) {
                            addressViewLayout.setVisibility(View.GONE);
                        } else {
                            addressViewLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w("FirestoreData", "Error getting documents.");
                    }
                });
    }

    private void addNewAddress() {
        database.collection("UserDetails")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        ArrayList<Address> addresses = (ArrayList<Address>) documentSnapshot.get("addresses");
                        Address address = new Address(addressLine.getText().toString().trim(),
                                                        city.getText().toString().trim(),
                                                        province.getText().toString().trim(),
                                                        postalCode.getText().toString().trim(),
                                                        country.getText().toString().trim());
                        addresses.add(address);

                        database.collection("UserDetails")
                                .document(user.getUid())
                                .update("addresses", addresses)
                                .addOnSuccessListener(aVoid -> {
                                    setAddressList();
                                    addressLine.setText("");
                                    city.setText("");
                                    province.setText("");
                                    postalCode.setText("");
                                    country.setText("");
                                    Log.d("Firestore", "Address updated successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error updating address", e);
                                });
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user details", e);
                });
    }

    private boolean isAddAddressFormValid() {
        boolean isValid = true;

        if(addressLine.getText().toString().trim().isEmpty()) {
            addressLine.setError("Address line cannot be empty");
            isValid = false;
        }

        if(city.getText().toString().trim().isEmpty()) {
            city.setError("City cannot be empty");
            isValid = false;
        }

        if(province.getText().toString().trim().isEmpty()) {
            province.setError("Province cannot be empty");
            isValid = false;
        }

        String postalCode = this.postalCode.getText().toString().trim();
        if(postalCode.isEmpty()) {
            this.postalCode.setError("Postal code cannot be empty");
            isValid = false;
        } else if (postalCode.length() < 5 || postalCode.length() > 10) {
            this.postalCode.setError("Postal code must be between 5 and 10 characters");
            isValid = false;
        }

        if(country.getText().toString().trim().isEmpty()) {
            country.setError("Country code cannot be empty");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onDataChanged() {
        setAddressList();
    }
}