package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.Home;
import com.example.project.ItemAdapter;
import com.example.project.Perfume;
import com.example.project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Woman extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private ImageButton homeButton, AddPerfume, CartButton, Search,Profile;
    private EditText searchEditText;
    private List<Perfume> perfumeList;
    private String userRole;
    private String userName;
    public boolean isSearchVisible = false;

    private static final int ADD_PERFUME_REQUEST_CODE = 1001;
    private static final int YOUR_EDIT_REQUEST_CODE = 1002;
    private DatabaseReference womenPerfumesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_woman);

        womenPerfumesRef = FirebaseDatabase.getInstance().getReference().child("perfumes").child("women");

        perfumeList = new ArrayList<>();
        recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        userRole = getIntent().getStringExtra("role");
        String userId = getIntent().getStringExtra("userId");
        userName=getIntent().getStringExtra("name");
        adapter = new ItemAdapter(this, perfumeList, recyclerView, userRole, userId);
        recyclerView.setAdapter(adapter);

        homeButton = findViewById(R.id.HomeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        CartButton=findViewById(R.id.CartPage);
        CartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Woman.this, Cart.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
        Search=findViewById(R.id.Search);
        searchEditText = findViewById(R.id.searchEditText);


        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the visibility of searchEditText
                isSearchVisible = !isSearchVisible;
                searchEditText.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);
            }
        });
        searchEditText.setVisibility(View.GONE);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterData(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Profile=findViewById(R.id.Profile);
        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileDialog();
            }
        });



        AddPerfume = findViewById(R.id.AddPerfume);


        if (userRole.equals("admin")) {
            AddPerfume.setVisibility(View.VISIBLE);
            CartButton.setVisibility(View.GONE);
        } else {
            AddPerfume.setVisibility(View.GONE);
            CartButton.setVisibility(View.VISIBLE);
        }

        AddPerfume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Woman.this, AddPerfumePage.class);
                intent.putExtra("category", "women");
                startActivityForResult(intent, ADD_PERFUME_REQUEST_CODE);
            }
        });

        readDataFromFirebase();
    }
    private void filterData(String query) {
        List<Perfume> filteredList = new ArrayList<>();

        for (Perfume perfume : perfumeList) {
            if (perfume.getPerfumeName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(perfume);
            }
        }

        adapter.setItems(filteredList, userRole);
    }


    private void readDataFromFirebase() {
        womenPerfumesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Perfume> perfumeList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Perfume perfume = snapshot.getValue(Perfume.class);
                    perfumeList.add(perfume);
                }
                String userRole = getIntent().getStringExtra("role");

                adapter.setItems(perfumeList, userRole);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PERFUME_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                readDataFromFirebase();
            }
        }else if (requestCode == YOUR_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Check if the result contains updated data
            Perfume updatedPerfume = data.getParcelableExtra("updatedPerfume");
            if (updatedPerfume != null) {
                adapter.updatePerfume(updatedPerfume);
                adapter.notifyDataSetChanged();

            }
        }
    }

    private void showProfileDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.profile_dialog);
        dialog.setTitle("Profile");

        TextView profileUsername = dialog.findViewById(R.id.profileUsername);
        TextView logoutButton = dialog.findViewById(R.id.logoutButton);

        // Set the username in the dialog
        profileUsername.setText(userName);



        // Set onClickListener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Woman.this, LoginPage.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                dialog.dismiss();
            }
        });

        View profileButton = findViewById(R.id.Profile);
        int[] location = new int[2];
        profileButton.getLocationOnScreen(location);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = location[0] + profileButton.getWidth() / 2-165;
        params.y = location[1] + profileButton.getHeight()-70;
        dialog.getWindow().setAttributes(params);

        // Show the dialog
        dialog.show();
    }


}