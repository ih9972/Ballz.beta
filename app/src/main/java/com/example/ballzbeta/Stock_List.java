package com.example.ballzbeta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballzbeta.objects.Item;
import com.example.ballzbeta.objects.WarehouseItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Stock_List extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WarehouseAdapter adapter;
    private List<WarehouseItem> warehouseItems;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private Uri imageUri = null;
    private final String defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/ballz-beta.firebasestorage.app/o/no_image_icon_23494.png?alt=media&token=083b4b39-9205-4f93-907c-ef823acab8d3";

    private String pendingName = "", pendingDesc = "";
    private int pendingAmount = 0;
    private boolean isWaitingForImage = false;
    private String companyId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_stock);
        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_messages) {
                    startActivity(new Intent(getApplicationContext(), Messages.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else {
                    return true;
                }
            }
        });

        recyclerView = findViewById(R.id.equipmentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        warehouseItems = new ArrayList<>();
        adapter = new WarehouseAdapter(this, warehouseItems);
        recyclerView.setAdapter(adapter);

        findUserCompanyAndLoadData();
    }

    private void findUserCompanyAndLoadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot companySnap : snapshot.getChildren()) {
                if (companySnap.hasChild(uid)) {
                    companyId = companySnap.getKey();
                    loadWarehouseItems(companyId);
                    break;
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to find company: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadWarehouseItems(String companyId) {
        FirebaseDatabase.getInstance().getReference("Admin")
                .child(companyId)
                .child("Wearhouse")
                .get()
                .addOnSuccessListener(snapshot -> {
                    warehouseItems.clear();
                    for (DataSnapshot itemSnap : snapshot.getChildren()) {
                        WarehouseItem item = itemSnap.getValue(WarehouseItem.class);
                        warehouseItems.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void AddItem(View view) {
        showAddItemDialog();
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);

        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputAmount = dialogView.findViewById(R.id.inputAmount);
        EditText inputDesc = dialogView.findViewById(R.id.inputDesc);
        Button selectImageBtn = dialogView.findViewById(R.id.selectImageBtn);

        imageUri = null;
        isWaitingForImage = false;

        selectImageBtn.setOnClickListener(v -> {
            String[] options = {"Choose from Gallery", "Take a Photo"};
            new AlertDialog.Builder(this)
                    .setTitle("Select Image")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            galleryIntent.setType("image/*");
                            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                        } else {
                            File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
                            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    })
                    .show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add New Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    pendingName = inputName.getText().toString().trim();
                    String amountStr = inputAmount.getText().toString().trim();
                    pendingDesc = inputDesc.getText().toString().trim();

                    if (pendingName.isEmpty() || amountStr.isEmpty() || companyId == null) {
                        Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pendingAmount = Integer.parseInt(amountStr);
                    isWaitingForImage = true;

                    if (imageUri != null) {
                        uploadImageAndSaveItem();
                    } else {
                        saveItem(defaultImageUrl);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadImageAndSaveItem() {
        String fileName = "images/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(uri -> saveItem(uri.toString()))
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveItem(String imageUrl) {
        Item item = new Item(0, pendingName, imageUrl, pendingDesc);
        WarehouseItem warehouseItem = new WarehouseItem(0, item, 1, pendingAmount); // present = 1 (available), total = quantity

        String itemId = FirebaseDatabase.getInstance()
                .getReference("Admin")
                .child(companyId)
                .child("Wearhouse")
                .push()
                .getKey();

        FirebaseDatabase.getInstance().getReference("Admin")
                .child(companyId)
                .child("Wearhouse")
                .child(itemId)
                .setValue(warehouseItem)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Item saved to Wearhouse", Toast.LENGTH_SHORT).show();
                    warehouseItems.add(warehouseItem);
                    adapter.notifyItemInserted(warehouseItems.size() - 1);
                    isWaitingForImage = false;
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }
}
