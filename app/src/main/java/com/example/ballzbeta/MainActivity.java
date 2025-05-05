package com.example.ballzbeta;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ballzbeta.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

public class MainActivity extends AppCompatActivity {
    EditText EmailET;
    EditText PasswordET;
    Context context;
    String current_user_id;
    Intent si;
    int role;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EmailET = findViewById(R.id.EmailET);
        PasswordET = findViewById(R.id.PasswordET);
        context = this;
        mAuth = FirebaseAuth.getInstance();
    }

    public void goto_signup(View view) {
        si = new Intent(this, Sign_up.class);
        startActivity(si);
    }

    public void login(View view) {
        String email = EmailET.getText().toString().trim();
        String password = PasswordET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            current_user_id = user.getUid();

                            FBRef.refUsers.get().addOnCompleteListener(usersTask -> {
                                if (usersTask.isSuccessful()) {
                                    DataSnapshot companiesSnap = usersTask.getResult();
                                    boolean found = false;

                                    for (DataSnapshot companySnap : companiesSnap.getChildren()) {
                                        if (companySnap.hasChild(current_user_id)) {
                                            DataSnapshot userSnap = companySnap.child(current_user_id);
                                            role = userSnap.child("role").getValue(Integer.class);

                                            Log.d("USER_ROLE", "Role is: " + role);

                                            Intent homeIntent = new Intent(context, Messages.class);
                                            homeIntent.putExtra("role", (role == 2 || role == 4) ? 0 : 1);
                                            startActivity(homeIntent);
                                            finish();
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to read user data", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(context, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
//u123456a