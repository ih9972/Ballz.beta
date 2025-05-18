package com.example.ballzbeta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
 * MainActivity handles user login with optional SharedPreferences-based
 * credential storage and role-based redirection.
 */
public class MainActivity extends AppCompatActivity {

    private EditText EmailET, PasswordET;
    private FirebaseAuth mAuth;
    private Context context;
    private String current_user_id;
    private int role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        EmailET = findViewById(R.id.EmailET);
        PasswordET = findViewById(R.id.PasswordET);
        mAuth = FirebaseAuth.getInstance();

        checkForSavedCredentials();
    }

    /**
     * Checks SharedPreferences for saved credentials and prompts the user to reuse them.
     */
    private void checkForSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        final String savedEmail = prefs.getString("email", null);
        final String savedPassword = prefs.getString("password", null);

        if (savedEmail != null && savedPassword != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Use Saved Login?")
                    .setMessage("Would you like to log in as: " + savedEmail + "?")
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
     * Called when the login button is clicked.
     *
     * @param view the login button
     */
    public void login(View view) {
        final String email = EmailET.getText().toString().trim();
        final String password = PasswordET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            current_user_id = user.getUid();

                            SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
                            String savedEmail = prefs.getString("email", null);

                            // Ask to save if it's a new account or not saved yet
                            if (savedEmail == null || !savedEmail.equals(email)) {
                                new AlertDialog.Builder(context)
                                        .setTitle("Save this account?")
                                        .setMessage("Do you want to save this login (" + email + ") for future use?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("email", email);
                                                editor.putString("password", password);
                                                editor.apply();
                                                loadUserRoleAndNavigate();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                loadUserRoleAndNavigate();
                                            }
                                        })
                                        .show();
                            } else {
                                loadUserRoleAndNavigate(); // already saved
                            }

                        } else {
                            Toast.makeText(context, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Logs in with credentials (auto-login) and continues to role detection.
     *
     * @param email    saved email
     * @param password saved password
     */
    private void loginWithCredentials(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            current_user_id = user.getUid();
                            loadUserRoleAndNavigate();
                        } else {
                            Toast.makeText(context, "Auto-login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Loads the current user's role from Firebase and navigates to Messages screen.
     * Sends a role flag to adjust downstream behavior (team vs warehouse).
     */
    private void loadUserRoleAndNavigate() {
        FBRef.refUsers.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot companiesSnap = task.getResult();
                    boolean found = false;

                    for (DataSnapshot companySnap : companiesSnap.getChildren()) {
                        if (companySnap.hasChild(current_user_id)) {
                            DataSnapshot userSnap = companySnap.child(current_user_id);
                            role = userSnap.child("role").getValue(Integer.class);

                            Intent intent = new Intent(context, Messages.class);
                            intent.putExtra("role", (role == 2 || role == 4) ? 0 : 1);
                            startActivity(intent);
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
            }
        });
    }

    /**
     * Navigates to the sign-up activity.
     *
     * @param view the "Sign up" button
     */
    public void goto_signup(View view) {
        startActivity(new Intent(this, Sign_up.class));
    }
}
