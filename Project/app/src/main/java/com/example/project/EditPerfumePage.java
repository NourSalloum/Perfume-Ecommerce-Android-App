package com.example.project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditPerfumePage extends AppCompatActivity {
    private ImageButton editImage;
    private ImageView editedImage;
    private EditText editPerfumeName, editPerfumePrice;
    private Button saveEdits, cancelEdit;
    private static final int GALLERY_REQUEST = 1889;
    private DatabaseReference perfumesRef;
    private String category;
    private Uri selectedImageUri;
    private String perfumeImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfume_page);

        // Initialize UI elements
        editImage = findViewById(R.id.edit_image);
        editedImage = findViewById(R.id.edited_image);
        editPerfumeName = findViewById(R.id.editperfume_name);
        editPerfumePrice = findViewById(R.id.editperfume_price);
        saveEdits = findViewById(R.id.save_edits);
        cancelEdit = findViewById(R.id.cancel_edit);

        // Get data passed from the previous activity (you may need to adjust this based on your data model)
        String perfumeName = getIntent().getStringExtra("perfumeName");
        double perfumePrice = getIntent().getDoubleExtra("perfumePrice", 0.0);
        String perfumeKey = getIntent().getStringExtra("perfumeKey");
        perfumeImageUrl = getIntent().getStringExtra("perfumeImageUrl");
        category = getIntent().getStringExtra("category");

        // Set the retrieved data to the UI elements
        editPerfumeName.setText(perfumeName);
        editPerfumePrice.setText(String.valueOf(perfumePrice));
        if (perfumeImageUrl != null && !perfumeImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(perfumeImageUrl)
                    .into(editedImage);
        }

        // Handle cancel button click
        cancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveEdits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the edited perfume details
                String editedName = editPerfumeName.getText().toString();
                double editedPrice = Double.parseDouble(editPerfumePrice.getText().toString());

                // Get the key passed from ItemAdapter
                String perfumeKey = getIntent().getStringExtra("perfumeKey");

                // Get a reference to the Perfume in the Realtime Database
                DatabaseReference perfumeRef = FirebaseDatabase.getInstance()
                        .getReference().child("perfumes").child(category).child(perfumeKey);

                if (selectedImageUri != null) {
                    // Get a reference to store the new image in Firebase Storage
                    StorageReference imageRef = FirebaseStorage.getInstance()
                            .getReference().child("perfume_images")
                            .child(selectedImageUri.getLastPathSegment());

                    // Upload the new image to Firebase Storage
                    UploadTask uploadTask = imageRef.putFile(selectedImageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save the updated perfume details along with the new image URL
                            String imageUrl = uri.toString();

                            // Update the perfume details
                            perfumeRef.child("perfumeName").setValue(editedName);
                            perfumeRef.child("price").setValue(editedPrice);
                            perfumeRef.child("imageResourceId").setValue(imageUrl);

                            // Notify the user that edits were saved successfully
                            Toast.makeText(EditPerfumePage.this, "Edits saved successfully", Toast.LENGTH_SHORT).show();

                            // Create a Perfume object with updated details
                            Perfume updatedPerfume = new Perfume();
                            updatedPerfume.setPerfumeName(editedName);
                            updatedPerfume.setprice(editedPrice);
                            updatedPerfume.setImageResourceId(imageUrl);
                            updatedPerfume.setKey(perfumeKey);

                            // Set result to indicate that edits were saved successfully
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updatedPerfume", updatedPerfume);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(EditPerfumePage.this, "Failed to upload new image", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // No new image selected, only update the perfume details
                    // Update the perfume details
                    perfumeRef.child("perfumeName").setValue(editedName);
                    perfumeRef.child("price").setValue(editedPrice);

                    // Notify the user that edits were saved successfully
                    Toast.makeText(EditPerfumePage.this, "Edits saved successfully", Toast.LENGTH_SHORT).show();

                    // Create a Perfume object with updated details
                    Perfume updatedPerfume = new Perfume();
                    updatedPerfume.setPerfumeName(editedName);
                    updatedPerfume.setprice(editedPrice);
                    updatedPerfume.setImageResourceId(perfumeImageUrl); // Use the existing image URL
                    updatedPerfume.setKey(perfumeKey);

                    // Set result to indicate that edits were saved successfully
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedPerfume", updatedPerfume);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        cancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST) {
            if (data != null) {
                selectedImageUri = data.getData();
                editedImage.setImageURI(selectedImageUri);
            }
        }
    }





}
