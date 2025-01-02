package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupPage extends AppCompatActivity {

    private Button Signup;
    private EditText user_name;
    private EditText user_password;
    private EditText user_email;
    TextView loginRedirectText;
    private ToggleButton ShowPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        user_name = findViewById(R.id.user_name);
        user_password = findViewById(R.id.user_passsword);
        user_email = findViewById(R.id.user_email);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        Signup = findViewById(R.id.next);
        ShowPass = findViewById(R.id.toggleButton);

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = user_name.getText().toString();
                String email = user_email.getText().toString();
                String password = user_password.getText().toString();
                String role = "user";

                if (username.equals("admin") && password.equals("123")) {
                    role = "admin";
                }

                // Generate a unique userId (you can use Firebase Authentication for this)
                String userId = generateUniqueId();

                // Create a User object with the userId
                User user = new User(userId, username, email, password, role);

                // Save the current user to SharedPreferences
                saveCurrentUser(user);

                // Save the user to Firebase
                saveUserToFirebase(user);

                Toast.makeText(SignupPage.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignupPage.this, LoginPage.class);
                startActivity(intent);
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupPage.this, LoginPage.class);
                startActivity(intent);
            }
        });
    }

    private void saveCurrentUser(User user) {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("currentUserId", user.getUserId());
        editor.putString("currentUsername", user.getUsername());
        editor.putString("currentUserRole", user.getRole());
        editor.apply();
    }

    private void saveUserToFirebase(User user) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference("user");
        myRef.child(user.getUserId()).setValue(user); // Use userId as the key
    }

    // This is a simple method to generate a unique user ID. You may want to replace it with Firebase Authentication.
    private String generateUniqueId() {
        return "user_" + System.currentTimeMillis();
    }
}
