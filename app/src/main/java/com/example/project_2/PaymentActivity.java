package com.example.project_2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentActivity extends AppCompatActivity {

    private Toolbar topToolbar;
    private EditText cardHolderName, cardNumber, expiryMonth, expiryYear, cvv;
    private Button paymentButton;
    private TextView amountView;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topToolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(topToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Checkout");
        }

        amount = getIntent().getDoubleExtra("amount", 0d);
        amountView = findViewById(R.id.amount);
        cardHolderName = findViewById(R.id.cardHolderName);
        cardNumber = findViewById(R.id.cardNumber);
        expiryMonth = findViewById(R.id.expiryMonth);
        expiryYear = findViewById(R.id.expiryYear);
        cvv = findViewById(R.id.cvv);
        paymentButton = findViewById(R.id.paymentButton);

        amountView.setText("$" + String.format("%.2f", amount));

        paymentButton.setOnClickListener(view -> {
            if (validateInputs()) {
                Intent intent = new Intent(this, ThankYouActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        // Validate Card Holder Name
        if (cardHolderName.getText().toString().isEmpty()) {
            cardHolderName.setError("Card Holder Name is required");
            return false;
        }

        // Validate Card Number
        String cardNumberText = cardNumber.getText().toString();
        if (cardNumberText.isEmpty()) {
            cardNumber.setError("Card Number is required");
            return false;
        }
        if (cardNumberText.length() != 16 || !cardNumberText.matches("\\d+")) {
            cardNumber.setError("Invalid Card Number");
            return false;
        }

        // Validate Expiry Month
        String expiryMonthText = expiryMonth.getText().toString();
        if (expiryMonthText.isEmpty()) {
            expiryMonth.setError("Expiry Month is required");
            return false;
        }
        int month = Integer.parseInt(expiryMonthText);
        if (month < 1 || month > 12) {
            expiryMonth.setError("Invalid Expiry Month");
            return false;
        }

        // Validate Expiry Year
        String expiryYearText = expiryYear.getText().toString();
        if (expiryYearText.isEmpty()) {
            expiryYear.setError("Expiry Year is required");
            return false;
        }
        int year = Integer.parseInt(expiryYearText);
        if (year < 2024) {
            expiryYear.setError("Card Expired");
            return false;
        }

        // Validate CVV
        String cvvText = cvv.getText().toString();
        if (cvvText.isEmpty()) {
            cvv.setError("CVV is required");
            return false;
        }
        if (cvvText.length() != 3 || !cvvText.matches("\\d+")) {
            cvv.setError("Invalid CVV");
            return false;
        }

        return true;
    }
}