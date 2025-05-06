package com.example.ballzbeta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballzbeta.objects.WarehouseItem;

import java.util.List;

/**
 * WarehouseAdapter is a custom RecyclerView adapter that displays warehouse inventory items.
 * It binds {@link WarehouseItem} objects to the item_stock.xml layout, showing the item name,
 * total quantity, description, and image.
 */
public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.ViewHolder> {

    private Context context;
    private List<WarehouseItem> warehouseItems;

    /**
     * Constructs the adapter with the context and list of warehouse items.
     *
     * @param context         The context of the parent activity.
     * @param warehouseItems  The list of items to display.
     */
    public WarehouseAdapter(Context context, List<WarehouseItem> warehouseItems) {
        this.context = context;
        this.warehouseItems = warehouseItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WarehouseItem item = warehouseItems.get(position);

        holder.itemName.setText(item.getItem().getName());
        holder.itemAmount.setText("Amount: " + item.getTotal());
        holder.itemDescription.setText("Description: " + item.getItem().getDescription());

        Glide.with(context)
                .load(item.getItem().getImageUri())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return warehouseItems.size();
    }

    /**
     * ViewHolder class that holds and binds the UI components for each item view.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemAmount, itemDescription;
        ImageView itemImage;

        /**
         * Initializes the item view components.
         *
         * @param itemView The root view of the item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemAmount = itemView.findViewById(R.id.itemAmount);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemImage = itemView.findViewById(R.id.itemImage);
        }
    }
}
