package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Home extends AppCompatActivity {
    private Button Explore_WomanPage;
    private Button Explore_ManPage;
    private Button Explore_BodyPage;
    private String userId;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        name=getIntent().getStringExtra("name");
        Explore_WomanPage = findViewById(R.id.Woman_page);
        Explore_ManPage = findViewById(R.id.Man_page);
        Explore_BodyPage = findViewById(R.id.Body_page);
        userId = getIntent().getStringExtra("userId");

        Log.d("Home", "name: " + name);
        Explore_WomanPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Woman.class);
                intent.putExtra("role", getIntent().getStringExtra("role"));
                intent.putExtra("userId", userId);
                intent.putExtra("name", name );


                startActivity(intent);
            }
        });

        Explore_ManPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Man.class);
                intent.putExtra("role", getIntent().getStringExtra("role"));
                intent.putExtra("userId", userId);
                intent.putExtra("name", name );
                startActivity(intent);
            }
        });

        Explore_BodyPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Body.class);
                intent.putExtra("role", getIntent().getStringExtra("role"));
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }
}
