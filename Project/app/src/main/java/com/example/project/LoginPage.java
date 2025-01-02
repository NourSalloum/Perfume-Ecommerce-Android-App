package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginPage extends AppCompatActivity {
    private Button Next;
    private EditText user_name;
    private EditText user_password;
    TextView signupRedirectText;
    private ToggleButton ShowPass;
    private int savedCursorPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user_name = findViewById(R.id.user_name);
        user_password = findViewById(R.id.user_passsword);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        Next = findViewById(R.id.next);
        ShowPass = findViewById(R.id.toggleButton);

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() | !validatePassword()) {
                    Toast.makeText(LoginPage.this, "Your username or password is wrong", Toast.LENGTH_SHORT).show();
                } else {
                    checkUser();
                }
            }
        });

        ShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savedCursorPosition = user_password.getSelectionStart();

                if (isChecked) {
                    ShowPass.setBackgroundResource(R.drawable.hide);
                    user_password.setTransformationMethod(null);
                } else {
                    ShowPass.setBackgroundResource(R.drawable.eye);
                    user_password.setTransformationMethod(new PasswordTransformationMethod());
                }
                user_password.setSelection(savedCursorPosition);
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, SignupPage.class);
                startActivity(intent);
            }
        });
    }

    public void checkUser() {
        String userUsername = user_name.getText().toString().trim();
        String userPassword = user_password.getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User retrievedUser = userSnapshot.getValue(User.class);

                        if (retrievedUser != null && retrievedUser.getPassword().equals(userPassword)) {
                            // Authentication successful
                            // Extract user details
                            String nameFromDB = retrievedUser.getUsername();
                            String emailFromDB = retrievedUser.getEmail();
                            String usernameFromDB = retrievedUser.getUsername();
                            String passwordFromDB = retrievedUser.getPassword();
                            String userroleFromDB = retrievedUser.getRole();
                            String userIdFromDB = retrievedUser.getUserId();

                            Intent intent = new Intent(LoginPage.this, Home.class);
                            intent.putExtra("name", nameFromDB);
                            Log.d("LoginPage", "nameFromDB: " + nameFromDB);

                            intent.putExtra("password", passwordFromDB);
                            intent.putExtra("role", userroleFromDB);
                            intent.putExtra("userId", userIdFromDB);
                            startActivity(intent);
                            finish();
                        } else {
                            user_password.setError("Invalid Credentials");
                            user_password.requestFocus();
                        }
                    }
                } else {
                    user_name.setError("User does not exist");
                    user_name.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    public Boolean validateUsername() {
        String val = user_name.getText().toString();
        if (val.isEmpty()) {
            user_name.setError("Username cannot be empty");
            return false;
        } else {
            user_name.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = user_password.getText().toString();
        if (val.isEmpty()) {
            user_password.setError("Password cannot be empty");
            return false;
        } else {
            user_password.setError(null);
            return true;
        }
    }
}
