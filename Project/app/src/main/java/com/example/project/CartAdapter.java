package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<Perfume> cartItemList;
    private CartListener cartListener;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private Map<String, Integer> quantityMap = new HashMap<>();

    public CartAdapter(Context context, List<Perfume> cartItemList, CartListener cartListener, RecyclerView recyclerView) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.cartListener = cartListener;
        this.recyclerView = recyclerView;

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Initialize the quantity map with stored values from SharedPreferences
        for (Perfume perfume : cartItemList) {
            String key = perfume.getKey();
            int storedQuantity = getStoredQuantity(key);
            quantityMap.put(key, storedQuantity);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Perfume perfume = cartItemList.get(position);

        holder.cartItemName.setText(perfume.getPerfumeName());
        holder.cartItemPrice.setText("$" + String.format(Locale.getDefault(), "%.2f", perfume.getprice()));
        Glide.with(context).load(perfume.getImageResourceId()).into(holder.cartItemImage);

        int quantity = getQuantity(perfume.getKey());
        holder.quantityText.setText(String.valueOf(quantity));

        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQuantity = quantity + 1;
                setQuantity(perfume.getKey(), newQuantity);
                holder.quantityText.setText(String.valueOf(newQuantity));
                notifyItemChanged(position);
                cartListener.onQuantityChanged(position, newQuantity);
                saveQuantity(perfume.getKey(), newQuantity);
            }
        });

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = getQuantity(perfume.getKey());
                if (currentQuantity > 1) {
                    int newQuantity = currentQuantity - 1;
                    setQuantity(perfume.getKey(), newQuantity);
                    holder.quantityText.setText(String.valueOf(newQuantity));
                    notifyItemChanged(position);
                    cartListener.onQuantityChanged(position, newQuantity);
                    saveQuantity(perfume.getKey(), newQuantity);
                }
            }
        });

        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    cartListener.onItemDeleted(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView cartItemImage;
        public TextView cartItemName;
        public TextView cartItemPrice;
        public TextView quantityText;
        public ImageButton plus;
        public ImageButton minus;
        public ImageButton Delete;

        public ViewHolder(View itemView) {
            super(itemView);
            cartItemImage = itemView.findViewById(R.id.Cart_Image);
            cartItemName = itemView.findViewById(R.id.CartName);
            cartItemPrice = itemView.findViewById(R.id.CartPrice);
            quantityText = itemView.findViewById(R.id.quantity);
            plus = itemView.findViewById(R.id.plus);
            minus = itemView.findViewById(R.id.minus);
            Delete = itemView.findViewById(R.id.Cart_Delete);
        }
    }

    public interface CartListener {
        void onQuantityChanged(int position, int quantity);
        void onItemDeleted(int position);
    }

    private int getStoredQuantity(String key) {
        int storedQuantity = sharedPreferences.getInt(key, 1);
        Log.d("CartAdapter", "Stored quantity for key " + key + ": " + storedQuantity);
        return storedQuantity;
    }


    private void saveQuantity(String key, int quantity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, quantity);
        editor.apply();
        Log.d("CartAdapter", "Saved quantity for key " + key + ": " + quantity);
    }

    public int getQuantity(String key) {
        return quantityMap.get(key) != null ? quantityMap.get(key) : 1;
    }

    public void setQuantity(String key, int quantity) {
        quantityMap.put(key, quantity);
    }

    public ViewHolder getViewHolder(int position) {
        return (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
    }
}
