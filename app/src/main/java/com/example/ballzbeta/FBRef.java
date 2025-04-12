package com.example.ballzbeta;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef{
    public static FirebaseDatabase FBRef = FirebaseDatabase.getInstance();
    public static DatabaseReference refRoles = FBRef.getReference("Roles");
    public static DatabaseReference refComp = FBRef.getReference("Admin");
    public static DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference storageRef = storage.getReference();

    public static StorageReference refST = storage.getReference();
    public static StorageReference refStamp = refST.child("Stamps");
}
