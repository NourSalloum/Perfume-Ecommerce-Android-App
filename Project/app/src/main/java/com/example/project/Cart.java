// Cart.java
package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Cart extends AppCompatActivity implements CartAdapter.CartListener {

    private RecyclerView recyclerViewCart;
    private TextView textViewTotal;
    private List<Perfume> cartItemList;
    private CartAdapter cartAdapter;
    private String userId;
    private ImageButton Back;

    private Button CheckOut;
    private Map<String, Integer> quantityMap; // Map to store quantities

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the quantity map and other necessary data
        outState.putSerializable("quantityMap", (HashMap<String, Integer>) quantityMap);
        // Add other data you want to persist
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        userId = getIntent().getStringExtra("userId");

        recyclerViewCart = findViewById(R.id.CartRecyclerView);
        textViewTotal = findViewById(R.id.Total);
        Back = findViewById(R.id.Back);
        CheckOut=findViewById(R.id.next);
        cartItemList = new ArrayList<>();

        // Initialize the quantity map with an empty map
        quantityMap = new HashMap<>();

        // Check if there's a saved instance state and restore data
        if (savedInstanceState != null) {
            HashMap<String, Integer> savedQuantityMap = (HashMap<String, Integer>) savedInstanceState.getSerializable("quantityMap");
            if (savedQuantityMap != null) {
                quantityMap.putAll(savedQuantityMap);
                Log.d("CartActivity", "Restored quantity map: " + quantityMap.toString());
            }
            // Restore other data if needed
        }


        cartAdapter = new CartAdapter(this, cartItemList, this, recyclerViewCart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);

        fetchCartData();

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        CheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Cart.this, PaymentPage.class);
                intent.putExtra("userId", userId); // Make sure userId is not null
                startActivity(intent);
            }
        });
    }

    private void fetchCartData() {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("carts").child(userId);

        cartRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Perfume perfume = dataSnapshot.getValue(Perfume.class);

                // Check if the perfume is already in the cartItemList
                if (!cartItemList.contains(perfume)) {
                    cartItemList.add(perfume);
                }

                // Check if the key is already present in quantityMap
                String key = perfume.getKey();
                int storedQuantity = quantityMap.containsKey(key) ? quantityMap.get(key) : 1;
                quantityMap.put(key, storedQuantity);

                cartAdapter.notifyDataSetChanged();
                updateTotal();
                Log.d("CartAdapter", "Quantity added for key " + key + ": " + storedQuantity);
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Perfume updatedPerfume = dataSnapshot.getValue(Perfume.class);
                String key = dataSnapshot.getKey();
                int index = findPerfumeIndexByKey(key);

                if (index != -1) {
                    cartItemList.set(index, updatedPerfume);
                    cartAdapter.notifyItemChanged(index);
                    updateTotal();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = findPerfumeIndexByKey(key);

                if (index != -1) {
                    cartItemList.remove(index);
                    quantityMap.remove(key); // Remove quantity for the deleted item
                    cartAdapter.notifyItemRemoved(index);
                    updateTotal();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // Handle moved items
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    @Override
    public void onQuantityChanged(int position, int quantity) {
        Perfume perfume = cartItemList.get(position);
        quantityMap.put(perfume.getKey(), quantity);
        updateTotal();
        Log.d("CartAdapter", "Quantity changed for key " + perfume.getKey() + ": " + quantity);
    }

    @Override
    public void onItemDeleted(int position) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("carts").child(userId);

        Perfume deletedPerfume = cartItemList.get(position);
        cartRef.child(deletedPerfume.getKey()).removeValue();

        cartItemList.remove(position);
        quantityMap.remove(deletedPerfume.getKey()); // Remove quantity for the deleted item
        cartAdapter.notifyItemRemoved(position);
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (Perfume perfume : cartItemList) {
            String key = perfume.getKey();
            int quantity = quantityMap.get(key); // Retrieve quantity from the map
            total += perfume.getprice() * quantity;
        }
        if (total > 0) {
            CheckOut.setVisibility(View.VISIBLE);
        } else {
            CheckOut.setVisibility(View.GONE);
        }
        textViewTotal.setText(" $" + String.format(Locale.getDefault(), "%.2f", total));
    }

    private int findPerfumeIndexByKey(String key) {
        for (int i = 0; i < cartItemList.size(); i++) {
            Perfume perfume = cartItemList.get(i);
            if (perfume.getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }
}
