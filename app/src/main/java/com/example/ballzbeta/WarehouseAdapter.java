package com.example.ballzbeta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ballzbeta.objects.WarehouseItem;
import java.util.List;

/**
 * Adapter for displaying warehouse items in a RecyclerView.
 */
public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.ViewHolder> {

    private Context context;
    private List<WarehouseItem> warehouseItems;
    private OnItemLongClickListener longClickListener;

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

        holder.itemImage.setOnClickListener(v -> showImageDialog(item.getItem().getImageUri()));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return warehouseItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemAmount, itemDescription;
        ImageView itemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemAmount = itemView.findViewById(R.id.itemAmount);
            itemDescription = itemView.findViewById(R.id.item_description);
            itemImage = itemView.findViewById(R.id.itemImage);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(WarehouseItem item, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    private void showImageDialog(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image_preview, null);
        ImageView fullImage = dialogView.findViewById(R.id.fullImage);
        Glide.with(context).load(imageUrl).into(fullImage);
        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
