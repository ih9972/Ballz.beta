package com.example.ballzbeta;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballzbeta.objects.WarehouseItem;

import java.util.ArrayList;
import java.util.List;

public class WarehouseRequestAdapter extends RecyclerView.Adapter<WarehouseRequestAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private final List<WarehouseItem> itemList;
    private final List<Integer> filteredIndexes;

    public WarehouseRequestAdapter(Context context, List<WarehouseItem> items) {
        this.context = context;
        this.itemList = items;
        this.filteredIndexes = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            filteredIndexes.add(i);
            items.get(i).setTotal(0);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemDescription;
        EditText itemAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemAmount = itemView.findViewById(R.id.itemAmount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock_editable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int actualIndex = filteredIndexes.get(position);
        WarehouseItem item = itemList.get(actualIndex);

        holder.itemName.setText(item.getItem().getName());
        holder.itemDescription.setText(item.getItem().getDescription());

        if (holder.itemAmount.getTag() instanceof TextWatcher) {
            holder.itemAmount.removeTextChangedListener((TextWatcher) holder.itemAmount.getTag());
        }

        holder.itemAmount.setText(item.getTotal() > 0 ? String.valueOf(item.getTotal()) : "");
        holder.itemAmount.setHint("0");

        Glide.with(context)
                .load(item.getItem().getImageUri())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.itemImage);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    item.setTotal(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    item.setTotal(0);
                }
            }
        };

        holder.itemAmount.addTextChangedListener(watcher);
        holder.itemAmount.setTag(watcher);
    }

    @Override
    public int getItemCount() {
        return filteredIndexes.size();
    }

    public List<WarehouseItem> getAllItems() {
        return itemList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Integer> filtered = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    for (int i = 0; i < itemList.size(); i++) filtered.add(i);
                } else {
                    String filter = constraint.toString().toLowerCase().trim();
                    for (int i = 0; i < itemList.size(); i++) {
                        String name = itemList.get(i).getItem().getName();
                        if (name != null && name.toLowerCase().contains(filter)) {
                            filtered.add(i);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredIndexes.clear();
                if (results.values != null) {
                    filteredIndexes.addAll((List<Integer>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }
}
