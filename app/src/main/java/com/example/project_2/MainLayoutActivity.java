package com.example.project_2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainLayoutActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar topToolbar;
    private FirebaseAuth firebaseAuth;

    private static final String FIRST_TIME_KEY = "FIRST_TIME_KEY";
    private static final String PREFS_NAME = "PREFS_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        topToolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(topToolbar);
        firebaseAuth = FirebaseAuth.getInstance();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.productsFragment) {
                    setToolbarTitle(itemId);
                    loadFragment(new ProductsFragment(), false);
                } else if(itemId == R.id.cartFragment) {
                    setToolbarTitle(itemId);
                    loadFragment(new CartFragment(), false);
                } else if(itemId == R.id.userFragment) {
                    firebaseAuth.signOut();
                    Intent intent = new Intent(MainLayoutActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                return true;
            }
        });

        if(savedInstanceState == null) {
            loadFragment(new ProductsFragment(), true);
        }
        int itemId = bottomNavigationView.getSelectedItemId();
        setToolbarTitle(itemId);
    }

    private void setToolbarTitle(int itemId) {
        if (getSupportActionBar() != null) {
            if(itemId == R.id.productsFragment) {
                getSupportActionBar().setTitle("Home");
            } else if(itemId == R.id.cartFragment) {
                getSupportActionBar().setTitle("Cart");
            }
        }
    }

    private void loadFragment(Fragment fragment, boolean isViewInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(isViewInitialized) {
            fragmentTransaction.add(R.id.frame_layout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frame_layout, fragment);
        }
        fragmentTransaction.commit();
    }
}