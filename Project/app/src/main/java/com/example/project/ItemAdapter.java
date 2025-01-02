package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.Perfume;
import com.example.project.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import android.os.Handler;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Perfume> perfumeList;
    private Context context;
    private RecyclerView recyclerView;
    private String userRole;
    private SharedPreferences cartPreferences;
    private String userId; // Add user ID field

    // Initialize userRole and userId in the constructor
    public ItemAdapter(Context context, List<Perfume> perfumeList, RecyclerView recyclerView, String userRole, String userId) {
        this.perfumeList = perfumeList;
        this.context = context;
        this.recyclerView = recyclerView;
        this.userRole = userRole; // Initialize userRole
        this.userId = userId;     // Initialize userId
        cartPreferences = context.getSharedPreferences("cart", Context.MODE_PRIVATE);
    }

    public void updatePerfume(Perfume updatedPerfume) {
        for (int i = 0; i < perfumeList.size(); i++) {
            Perfume currentPerfume = perfumeList.get(i);
            if (currentPerfume.getKey().equals(updatedPerfume.getKey())) {
                perfumeList.set(i, updatedPerfume);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Perfume perfume = perfumeList.get(position);

        holder.itemNameTextView.setText(perfume.getPerfumeName());
        holder.itemPriceTextView.setText(String.valueOf(perfume.getprice())); // Convert double to String
        Glide.with(context).load(perfume.getImageResourceId()).into(holder.itemImage);

        if (userRole.equals("admin")) {
            holder.Edit.setVisibility(View.VISIBLE);
            holder.Delete.setVisibility(View.VISIBLE);
            holder.addToCart.setVisibility(View.GONE);
        } else {
            holder.Edit.setVisibility(View.GONE);
            holder.Delete.setVisibility(View.GONE);
            holder.addToCart.setVisibility(View.VISIBLE);
        }

        holder.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the item to the user's cart in the Firebase database
                addToCart(perfume, userId);
            }
        });


        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < perfumeList.size()) {
                    Perfume perfume = perfumeList.get(position);

                    String category = perfume.getCategory();
                    DatabaseReference perfumeRef = FirebaseDatabase.getInstance().getReference().child("perfumes").child(category).child(perfume.getKey());

                    perfumeRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Remove the item from the list after successful deletion
                                perfumeList.remove(position);

                                // Use Handler to perform UI updates on the main thread
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Notify item removed
                                        notifyItemRemoved(position);

                                        // Notify item range changed to fix any issues with remaining items
                                        notifyItemRangeChanged(position, getItemCount());

                                        // Check if the list is empty after removal
                                        if (perfumeList.isEmpty()) {
                                            notifyDataSetChanged(); // Notify entire dataset change
                                        }

                                        // Force RecyclerView to redraw its views
                                        recyclerView.invalidate();
                                    }
                                });

                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "An error occurred while deleting", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        holder.Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(context, EditPerfumePage.class);
                editIntent.putExtra("perfumeName", perfume.getPerfumeName());
                editIntent.putExtra("perfumePrice", perfume.getprice());
                editIntent.putExtra("category", perfume.getCategory());
                editIntent.putExtra("perfumeImageUrl", perfume.getImageResourceId());
                editIntent.putExtra("perfumeKey", perfume.getKey());
                context.startActivity(editIntent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return perfumeList.size();
    }

    public void setItems(List<Perfume> perfumes, String userRole) {
        this.perfumeList = perfumes;
        this.userRole = userRole;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemNameTextView;
        public TextView itemPriceTextView;
        public ImageView itemImage;
        public Button addToCart, Edit;
        ImageButton Delete;

        public ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.Name);
            itemPriceTextView = itemView.findViewById(R.id.Price);
            itemImage = itemView.findViewById(R.id.Image);
            addToCart = itemView.findViewById(R.id.Cart);
            Delete = itemView.findViewById(R.id.Delete);
            Edit = itemView.findViewById(R.id.Edit);
        }
    }

    // Add the addToCart method
    private void addToCart(Perfume perfume, String userId) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("carts").child(userId).child(perfume.getKey());

        cartRef.setValue(perfume)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Item added to cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                });
    }
}
