package com.example.ballzbeta;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballzbeta.objects.Order;
import com.example.ballzbeta.objects.OrderItem;
import com.example.ballzbeta.objects.WarehouseItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class Messages extends AppCompatActivity {

    private EditText notesEditText, dateEditText, searchEditText;
    private Spinner receiverSpinner;
    private WarehouseRequestAdapter adapter;
    private List<WarehouseItem> warehouseItems = new ArrayList<>();
    private List<String> receiverNames = new ArrayList<>();
    private List<String> receiverUids = new ArrayList<>();
    private String companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_messages);
        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_stock) {
                    startActivity(new android.content.Intent(getApplicationContext(), Stock_List.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new android.content.Intent(getApplicationContext(), Profile.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return true;
            }
        });
    }

    public void Add_New_Order(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderUid = currentUser.getUid();

        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot companySnap : snapshot.getChildren()) {
                            if (companySnap.hasChild(senderUid)) {
                                companyId = companySnap.getKey();

                                receiverNames.clear();
                                receiverUids.clear();

                                for (DataSnapshot userSnap : companySnap.getChildren()) {
                                    Integer role = userSnap.child("role").getValue(Integer.class);
                                    String name = userSnap.child("name").getValue(String.class);
                                    if (role != null && (role == 1 || role == 3 || role == 4) && !userSnap.getKey().equals(senderUid)) {
                                        receiverNames.add(name);
                                        receiverUids.add(userSnap.getKey());
                                    }
                                }

                                if (receiverNames.isEmpty()) {
                                    Toast.makeText(Messages.this, "No warehouse workers found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                showRequestDialog(senderUid);
                                return;
                            }
                        }

                        Toast.makeText(Messages.this, "Company not found for current user", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Messages.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRequestDialog(String senderUid) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_order, null);

        notesEditText = dialogView.findViewById(R.id.notesEditText);
        dateEditText = dialogView.findViewById(R.id.dateEditText);
        receiverSpinner = dialogView.findViewById(R.id.receiverSpinner);
        searchEditText = dialogView.findViewById(R.id.searchEditText);
        Button submitButton = dialogView.findViewById(R.id.sendRequestButton);
        RecyclerView recyclerView = dialogView.findViewById(R.id.requestItemsRecyclerView);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, receiverNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        receiverSpinner.setAdapter(spinnerAdapter);

        adapter = new WarehouseRequestAdapter(this, warehouseItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editable) {
                adapter.getFilter().filter(editable.toString());
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog picker = new DatePickerDialog(Messages.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int y, int m, int d) {
                                dateEditText.setText(d + "/" + (m + 1) + "/" + y);
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                picker.show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrder(senderUid);
            }
        });

        DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                .getReference("Admin")
                .child(companyId)
                .child("Wearhouse");

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                warehouseItems.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    WarehouseItem item = itemSnap.getValue(WarehouseItem.class);
                    warehouseItems.add(item);
                }
                adapter = new WarehouseRequestAdapter(Messages.this, warehouseItems);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Messages.this, "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Create New Order")
                .setView(dialogView)
                .setCancelable(true)
                .show();
    }

    private void sendOrder(String senderUid) {
        String notes = notesEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a pickup date", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = receiverSpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= receiverUids.size()) {
            Toast.makeText(this, "Please select a worker", Toast.LENGTH_SHORT).show();
            return;
        }

        String receiverUid = receiverUids.get(selectedIndex);
        List<OrderItem> orderItems = new ArrayList<>();
        int itemId = 0;

        for (WarehouseItem wi : adapter.getAllItems()) {
            if (wi.getTotal() > 0) {
                orderItems.add(new OrderItem(itemId++, wi.getItem(), wi.getTotal()));
            }
        }

        if (orderItems.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        Order order = new Order(
                0,
                senderUid,
                receiverUid,
                notes,
                timestamp + " | Pickup: " + date,
                false,
                false,
                orderItems
        );

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Admin")
                .child(companyId)
                .child("Orders")
                .push();

        orderRef.setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Messages.this, "Order sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Messages.this, "Failed to send order", Toast.LENGTH_LONG).show();
            }
        });
    }
}
