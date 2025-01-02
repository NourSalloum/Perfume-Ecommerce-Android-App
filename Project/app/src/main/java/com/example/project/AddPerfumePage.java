package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddPerfumePage extends AppCompatActivity {
    private Button Save, Cancel;
    private ImageButton Image;
    private ImageView Perfume_Image;
    private EditText Perfume_name, Perfume_price;
    private static final int GALLERY_REQUEST = 1889;
    private DatabaseReference perfumesRef;
    private StorageReference storageRef;
    private String category;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_perfume_page);

        category = getIntent().getStringExtra("category");

        Perfume_Image = findViewById(R.id.added_image);
        Image = findViewById(R.id.add_image);
        Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        Save = findViewById(R.id.save);
        Cancel = findViewById(R.id.cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Perfume_name = findViewById(R.id.perfume_name);
                Perfume_price = findViewById(R.id.perfume_price);
                perfumesRef = FirebaseDatabase.getInstance().getReference().child("perfumes").child(category);
                storageRef = FirebaseStorage.getInstance().getReference().child("perfume_images");

                if (selectedImageUri != null) {
                    // Get a reference to store the image in Firebase Storage
                    StorageReference imageRef = storageRef.child(selectedImageUri.getLastPathSegment());

                    // Upload the image to Firebase Storage
                    UploadTask uploadTask = imageRef.putFile(selectedImageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String name = Perfume_name.getText().toString();
                            double price = Double.parseDouble(Perfume_price.getText().toString());
                            String imageUrl = uri.toString();

                            // Save perfume details along with the image URL in the Realtime Database
                            Perfume newPerfume = new Perfume();
                            newPerfume.setPerfumeName(name);
                            newPerfume.setprice(price);
                            newPerfume.setImageResourceId(imageUrl);
                            newPerfume.setCategory(category);

                            // Set the key for the new perfume
                            String newPerfumeKey = perfumesRef.push().getKey();
                            newPerfume.setKey(newPerfumeKey);

                            // Push the new perfume to the database
                            perfumesRef.child(newPerfumeKey).setValue(newPerfume)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddPerfumePage.this, "Perfume added successfully", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddPerfumePage.this, "Failed to add perfume", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddPerfumePage.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(AddPerfumePage.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST) {
            if (data != null) {
                selectedImageUri = data.getData();
                Perfume_Image.setImageURI(selectedImageUri);
            }
        }
    }
}
