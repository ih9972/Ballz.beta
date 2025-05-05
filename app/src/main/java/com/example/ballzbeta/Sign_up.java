package com.example.ballzbeta;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ballzbeta.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Sign_up extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText etName, etMail, etPassword;
    private Spinner comp_spinner, role_spinner;
    private Context context = this;
    private ArrayList<String> roles, companies, companyIds;
    private ArrayAdapter<String> roles_adp, companies_adp;

    private User user;
    private FirebaseAuth refAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        refAuth = FirebaseAuth.getInstance();
        initViews();
        fetchRolesAndCompanies();
    }

    private void initViews() {
        etName = findViewById(R.id.editTextName);
        etMail = findViewById(R.id.editTextEmailAddress);
        etPassword = findViewById(R.id.editTextPassword);
        role_spinner = findViewById(R.id.role_spinner);
        comp_spinner = findViewById(R.id.company_spinner);

        roles = new ArrayList<>();
        companies = new ArrayList<>();
        companyIds = new ArrayList<>();

        roles.add("collecting data...");
        companies.add("collecting data...");

        roles_adp = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, roles);
        role_spinner.setAdapter(roles_adp);
        role_spinner.setOnItemSelectedListener(this);

        companies_adp = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, companies);
        comp_spinner.setAdapter(companies_adp);
        comp_spinner.setOnItemSelectedListener(this);
    }

    private void fetchRolesAndCompanies() {
        // Fetch roles from Firebase
        FBRef.refRoles.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                roles.clear();
                for (DataSnapshot roleSnapshot : dataSnapshot.getChildren()) {
                    roles.add(roleSnapshot.getValue(String.class));
                }
                roles_adp.notifyDataSetChanged();
            } else {
                Log.e("Sign_up", "Failed to fetch roles");
            }
        });

        // Fetch companies from Firebase
        FBRef.FBRef.getReference("Admin").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                companies.clear();
                companyIds.clear();
                for (DataSnapshot companySnapshot : dataSnapshot.getChildren()) {
                    companies.add(companySnapshot.getKey());
                    companyIds.add(companySnapshot.getKey());
                }
                companies_adp.notifyDataSetChanged();
            } else {
                Log.e("Sign_up", "Failed to fetch companies");
            }
        });
    }

    public void signupbtn(View view) {
        String name = etName.getText().toString();
        String email = etMail.getText().toString();
        String password = etPassword.getText().toString();

        int rolePosition = role_spinner.getSelectedItemPosition();
        int companyPosition = comp_spinner.getSelectedItemPosition();
        String selectedCompanyId = companyIds.get(companyPosition);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Connecting");
        progressDialog.setMessage("Creating user...");
        progressDialog.show();

        refAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                String uid = refAuth.getCurrentUser().getUid();
                user = new User(uid, name, rolePosition, selectedCompanyId);

                // Save user to Firebase Realtime Database
                FBRef.refUsers.child(selectedCompanyId).child(uid).setValue(user)
                        .addOnCompleteListener(saveTask -> {
                            if (saveTask.isSuccessful()) {
                                Toast.makeText(context, "User created and saved", Toast.LENGTH_LONG).show();
                                navigateToHomePage(rolePosition);
                            } else {
                                Toast.makeText(context, "Failed to save user", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                handleFirebaseError(task.getException());
            }
        });
    }

    private void handleFirebaseError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException)
            Toast.makeText(context, "Invalid email address", Toast.LENGTH_LONG).show();
        else if (exception instanceof FirebaseAuthWeakPasswordException)
            Toast.makeText(context, "Password too weak", Toast.LENGTH_LONG).show();
        else if (exception instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(context, "User already exists", Toast.LENGTH_LONG).show();
        else if (exception instanceof FirebaseAuthInvalidCredentialsException)
            Toast.makeText(context, "General authentication failure", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "An error occurred, please try again later", Toast.LENGTH_LONG).show();
    }

    private void navigateToHomePage(int rolePosition) {
        Intent homeIntent = new Intent(context, Messages.class);
        if (rolePosition == 2 || rolePosition == 4) {
            homeIntent.putExtra("role", 0);
        } else {
            homeIntent.putExtra("role", 1);
        }
        startActivity(homeIntent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Handle spinner selections if necessary
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle no selection if necessary
    }
}
