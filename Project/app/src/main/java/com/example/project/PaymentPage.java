package com.example.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentPage extends AppCompatActivity {

    private Button payButton;
    private ImageButton Back;
    private EditText nameEditText, phoneEditText, addressEditText, cardNumberEditText,
            cardHolderEditText, cardCodeEditText;
    private String userId;
    private Spinner monthSpinner, yearSpinner;

    // Notification channel constants
    private static final String CHANNEL_ID = "payment_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_page);

        // Initialize UI elements
        nameEditText = findViewById(R.id.Name);
        phoneEditText = findViewById(R.id.Phone);
        addressEditText = findViewById(R.id.Address);
        cardNumberEditText = findViewById(R.id.CardNumber);
        cardHolderEditText = findViewById(R.id.CardHolder);
        cardCodeEditText = findViewById(R.id.CardCode);
        monthSpinner = findViewById(R.id.spinnerExpiryMonth);
        yearSpinner = findViewById(R.id.spinnerExpiryYear);
        userId = getIntent().getStringExtra("userId");
        payButton = findViewById(R.id.Pay);
        Back=findViewById(R.id.Back);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateUserInput()) {
                    deletePerfumesFromCart();
                    navigateBackToCartPage();

                }
            }
        });
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validateUserInput() {
        // Validate user input, return true if all fields are filled, otherwise show an error
        if (nameEditText.getText().toString().isEmpty() ||
                phoneEditText.getText().toString().isEmpty() ||
                addressEditText.getText().toString().isEmpty() ||
                cardNumberEditText.getText().toString().isEmpty() ||
                cardHolderEditText.getText().toString().isEmpty() ||
                cardCodeEditText.getText().toString().isEmpty() ||
                monthSpinner.getSelectedItemPosition() == 0 ||
                yearSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Add additional validation if needed
        return true;
    }

    private void deletePerfumesFromCart() {
        // Get a reference to the user's cart in the Firebase Realtime Database
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("carts").child(userId);

        // Remove all items from the user's cart
        cartRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showSuccessNotification();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Deletion failed
                        Toast.makeText(PaymentPage.this, "Failed to remove items from the cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void navigateBackToCartPage() {
        finish();
    }


    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name="Payment Channel";
            String Description="Channel for payment notifications";
            int importance= NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,name,importance);
            channel.setDescription(Description);

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    private void showSuccessNotification() {
        createNotificationChannel(); // Make sure the channel is created before showing the notification
        Notification.Builder builder=null;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            builder=new Notification.Builder(this,CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Payment Successful").setContentText("Dear Customer, your order will be delivered within a week.");
        }
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager!=null){
            notificationManager.notify(1,builder.build());
        }
    }


}
