package com.example.ballzbeta.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballzbeta.adapters.FulfillmentItemAdapter;
import com.example.ballzbeta.adapters.OrderItemAdapter;
import com.example.ballzbeta.R;
import com.example.ballzbeta.objects.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter to display order and notification messages in the Messages activity.
 * Handles message type labeling, sender/receiver resolution, and long-click dialogs
 * for both viewing and fulfilling orders.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<Order> messageList;
    private final Map<String, String> userMap = new HashMap<>();

    /**
     * ViewHolder for message items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageType, messageDirection, messageStatus, messageRemark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageType = itemView.findViewById(R.id.message_type);
            messageDirection = itemView.findViewById(R.id.message_direction);
            messageStatus = itemView.findViewById(R.id.message_status);
            messageRemark = itemView.findViewById(R.id.message_remark);
        }
    }

    public MessageAdapter(List<Order> messages) {
        this.messageList = messages;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Order order = messageList.get(position);
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        boolean isOrder = order.getOrderItemList() != null && !order.getOrderItemList().isEmpty();
        boolean isSystem = "system".equals(order.getSender());
        boolean isSender = currentUid.equals(order.getSender());

        // Set message type
        holder.messageType.setText(isOrder ? "[Order]" : "[Notification]");

        // Set direction label
        String directionText = "";
        String otherUid = null;
        if (!isSystem) {
            if (isSender) {
                directionText = "Sent to ";
                otherUid = order.getReceiver();
            } else {
                directionText = "Received from ";
                otherUid = order.getSender();
            }

            String name = userMap.getOrDefault(otherUid, otherUid);
            directionText += name;
        }
        holder.messageDirection.setText(directionText);

        // Set order status
        if (isOrder) {
            holder.messageStatus.setText(order.isDone() ? "Status: Completed" : "Status: In Progress");
        } else {
            holder.messageStatus.setText("");
        }

        // Set message remark
        holder.messageRemark.setText(order.getRemark() != null ? order.getRemark() : "");

        // Long-click behavior: open fulfillment or view dialog
        holder.itemView.setOnLongClickListener(v -> {
            if (!isOrder) return false;

            boolean isReceiver = currentUid.equals(order.getReceiver());

            if (isReceiver && !order.isDone()) {
                showFulfillmentDialog(v, order);
            } else {
                showViewOnlyDialog(v, order);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Shows the fulfillment dialog where warehouse staff can confirm packed amounts.
     */
    private void showFulfillmentDialog(View view, Order order) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_order_fulfillment, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.fulfillmentRecyclerView);
        TextView notesText = dialogView.findViewById(R.id.notesText);
        TextView pickupDateText = dialogView.findViewById(R.id.pickupDateText);
        Button doneButton = dialogView.findViewById(R.id.markDoneButton);

        notesText.setText("Notes: " + order.getRemark());

        String[] dateParts = order.getDateTime().split("\\|");
        String pickupOnly = dateParts.length > 1 ? dateParts[1].trim() : order.getDateTime();
        pickupDateText.setText("Pickup: " + pickupOnly);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new FulfillmentItemAdapter(order.getOrderItemList()));

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setTitle("Fulfill Order")
                .setView(dialogView)
                .setCancelable(false)
                .create();

        doneButton.setOnClickListener(btn -> {
            order.setDone(true);

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot companySnap : snapshot.getChildren()) {
                        if (companySnap.hasChild(currentUid)) {
                            String companyId = companySnap.getKey();
                            DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                                    .getReference("Admin")
                                    .child(companyId)
                                    .child("Orders");

                            ordersRef.orderByChild("dateTime").equalTo(order.getDateTime())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {
                                            for (DataSnapshot orderSnap : snap.getChildren()) {
                                                orderSnap.getRef().setValue(order);
                                                break;
                                            }
                                            int position = messageList.indexOf(order);
                                            if (position != -1) {
                                                messageList.set(position, order);
                                                notifyItemChanged(position);
                                            }
                                            dialog.dismiss();

                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle error
                                        }
                                    });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        });

        dialog.show();
    }

    /**
     * Shows a read-only dialog with full order details.
     */
    private void showViewOnlyDialog(View view, Order order) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_order_view_details, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.itemsRecyclerView);
        TextView notesText = dialogView.findViewById(R.id.orderNotes);
        TextView pickupDateText = dialogView.findViewById(R.id.pickupDate);
        TextView workerText = dialogView.findViewById(R.id.warehouseWorker);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new OrderItemAdapter(order.getOrderItemList()));

        notesText.setText("Notes: " + order.getRemark());
        String[] dateParts = order.getDateTime().split("\\|");
        String pickupOnly = dateParts.length > 1 ? dateParts[1].trim() : order.getDateTime();
        pickupDateText.setText(pickupOnly);

        String receiverName = userMap.getOrDefault(order.getReceiver(), order.getReceiver());
        workerText.setText("Warehouse Worker: " + receiverName);

        new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setTitle("Order Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * Loads user names from Firebase and maps them by UID.
     *
     * @param companyId The company ID under which the users are stored.
     */
    public void fetchUserNames(String companyId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(companyId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String name = userSnap.child("name").getValue(String.class);
                    if (uid != null && name != null) {
                        userMap.put(uid, name);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle user fetch failure
            }
        });
    }
}
