package com.example.ballzbeta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

/**
 * Handles login logic and optionally uses saved credentials from SharedPreferences.
 */
public class MainActivity extends AppCompatActivity {

    private EditText EmailET;
    private EditText PasswordET;
    private FirebaseAuth mAuth;
    private Context context;
    private String current_user_id;
    private int role;

    /**
     * Initializes FirebaseAuth, UI elements, and prompts to use saved credentials.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmailET = findViewById(R.id.EmailET);
        PasswordET = findViewById(R.id.PasswordET);
        context = this;
        mAuth = FirebaseAuth.getInstance();

        checkForSavedCredentials();
    }

    /**
     * Checks if saved email and password exist in SharedPreferences.
     * If so, prompts the user to auto-fill them using an AlertDialog.
     */
    private void checkForSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", null);
        String savedPassword = prefs.getString("password", null);

        if (savedEmail != null && savedPassword != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Use Saved Login?")
                    .setMessage("Would you like to log in using saved credentials?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            EmailET.setText(savedEmail);
                            PasswordET.setText(savedPassword);
                            loginWithCredentials(savedEmail, savedPassword);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    /**
     * Navigates to the SignUp screen.
     *
     * @param view Button click
     */
    public void goto_signup(View view) {
        startActivity(new Intent(this, Sign_up.class));
    }

    /**
     * Authenticates the user and saves email/password to SharedPreferences if successful.
     *
     * @param view Button click
     */
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

                            // Save only email and password in SharedPreferences
                            SharedPreferences.Editor editor = getSharedPreferences("user_credentials", MODE_PRIVATE).edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();

                            // Load user role and company info
                            FBRef.refUsers.get().addOnCompleteListener(usersTask -> {
                                if (usersTask.isSuccessful()) {
                                    DataSnapshot companiesSnap = usersTask.getResult();
                                    boolean found = false;

                                    for (DataSnapshot companySnap : companiesSnap.getChildren()) {
                                        if (companySnap.hasChild(current_user_id)) {
                                            DataSnapshot userSnap = companySnap.child(current_user_id);
                                            role = userSnap.child("role").getValue(Integer.class);

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
    private void loginWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
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
                        Toast.makeText(context, "Auto-login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
