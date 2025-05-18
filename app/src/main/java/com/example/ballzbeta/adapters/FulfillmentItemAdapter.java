package com.example.ballzbeta.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballzbeta.R;
import com.example.ballzbeta.objects.OrderItem;

import java.util.List;

/**
 * Adapter for displaying and editing fulfillment amounts for each OrderItem.
 */
public class FulfillmentItemAdapter extends RecyclerView.Adapter<FulfillmentItemAdapter.ViewHolder> {

    private final List<OrderItem> items;

    public FulfillmentItemAdapter(List<OrderItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, requestedAmount;
        EditText fulfilledAmountInput;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            requestedAmount = itemView.findViewById(R.id.requestedAmount);
            fulfilledAmountInput = itemView.findViewById(R.id.fulfilledAmountInput);
        }
    }

    @NonNull
    @Override
    public FulfillmentItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_fulfillment_editable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FulfillmentItemAdapter.ViewHolder holder, int position) {
        OrderItem item = items.get(position);

        holder.itemName.setText(item.getItem().getName());
        holder.requestedAmount.setText("Requested: " + item.getAmount());

        // Avoid previous text watcher interference
        if (holder.fulfilledAmountInput.getTag() instanceof TextWatcher) {
            holder.fulfilledAmountInput.removeTextChangedListener((TextWatcher) holder.fulfilledAmountInput.getTag());
        }

        holder.fulfilledAmountInput.setText(item.getFulfilledAmount() > 0 ? String.valueOf(item.getFulfilledAmount()) : "");

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int value = Integer.parseInt(s.toString().trim());
                    item.setFulfilledAmount(value);
                } catch (NumberFormatException e) {
                    item.setFulfilledAmount(0); // fallback to 0
                }
            }
        };

        holder.fulfilledAmountInput.addTextChangedListener(watcher);
        holder.fulfilledAmountInput.setTag(watcher);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
