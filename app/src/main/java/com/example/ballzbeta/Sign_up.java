package com.example.ballzbeta;

import static com.example.ballzbeta.FBRef.refAuth;
import static com.example.ballzbeta.FBRef.refComp;
import static com.example.ballzbeta.FBRef.refRoles;
import static com.example.ballzbeta.FBRef.storage;

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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ballzbeta.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.DataCollectionDefaultChange;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Sign_up extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
EditText etName,etMail,etPassword;
Spinner comp_spinner,role_spinner;
Context context = this;
ArrayList<String> roles, companies,companyIds ;
ArrayAdapter<String> roles_adp,companies_adp;

User user;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initviews();
        asyncTasks();
    }

    private void initviews() {
        etName = (EditText) findViewById(R.id.editTextName);
        etMail = (EditText) findViewById(R.id.editTextEmailAddress);
        etPassword = (EditText) findViewById(R.id.editTextPassword);
        role_spinner = (Spinner) findViewById(R.id.role_spinner);
        comp_spinner = (Spinner) findViewById(R.id.company_spinner);
        roles = new ArrayList<>();
        roles.add("collecting  data...");
        companies = new ArrayList<>();
        companyIds = new ArrayList<>();
        companies.add("collecting  data...");
        roles_adp = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,roles);
        role_spinner.setAdapter(roles_adp);
        role_spinner.setOnItemSelectedListener(this);
        companies_adp = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,companies);
        comp_spinner.setAdapter(companies_adp);
        comp_spinner.setOnItemSelectedListener(this);
    }

    private void asyncTasks() {
        refRoles.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dS = task.getResult();
                    roles.clear();
                    for(DataSnapshot data : dS.getChildren()){
                        roles.add((String) data.getValue());
                    }
                    roles_adp.notifyDataSetChanged();
                    role_spinner.setSelection(3);
                } else {
                    Log.e("Sign_up", "Couldn't read roles");
                }
            }
        });

        FBRef.FBRef.getReference("Admin").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dS = task.getResult();
                    companies.clear();
                    companyIds.clear();
                    for (DataSnapshot data : dS.getChildren()) {
                        companies.add(data.getKey());
                        companyIds.add(data.getKey());
                    }
                    companies_adp.notifyDataSetChanged();
                    comp_spinner.setSelection(0);
                } else {
                    Log.e("Sign_up", "Couldn't read companies");
                }
            }
        });
    }

    public void signupbtn(View view) {
        String name = etName.getText().toString(), email = etMail.getText().toString(), password = etPassword.getText().toString();
        int role = role_spinner.getSelectedItemPosition();
        int company = comp_spinner.getSelectedItemPosition();
        String selectedCompanyId = companyIds.get(company);
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fiil all fildes", Toast.LENGTH_LONG).show();
        } else {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting");
            pd.setMessage("Creating user...");
            pd.show();
            refAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.dismiss();
                            if (task.isSuccessful()) {
                                String uid = refAuth.getCurrentUser().getUid();
                                user = new User(uid, name, role, selectedCompanyId);
                                FBRef.refUsers.child(selectedCompanyId).child(uid).setValue(user)
                                        .addOnCompleteListener(saveTask -> {
                                            if (saveTask.isSuccessful()) {
                                                Toast.makeText(context, "User created and saved", Toast.LENGTH_LONG).show();
                                                Intent homeIntent = new Intent(context, Messages.class);
                                                if (role == 2 || role == 4) {
                                                    homeIntent.putExtra("role", 0);
                                                } else {
                                                    homeIntent.putExtra("role", 1);
                                                }
                                                startActivity(homeIntent);
                                                finish();
                                            } else {
                                                Toast.makeText(context, "Failed to save user", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Exception exp = task.getException();

                                if (exp instanceof FirebaseAuthInvalidUserException)
                                    Toast.makeText(context, "Invalid email address", Toast.LENGTH_LONG).show();
                                else if (exp instanceof FirebaseAuthWeakPasswordException)
                                    Toast.makeText(context, "Password too weak", Toast.LENGTH_LONG).show();
                                else if (exp instanceof FirebaseAuthUserCollisionException)
                                    Toast.makeText(context, "User already exists", Toast.LENGTH_LONG).show();
                                else if (exp instanceof FirebaseAuthInvalidCredentialsException)
                                    Toast.makeText(context, "General authentication failure", Toast.LENGTH_LONG).show();
                                else if (exp instanceof FirebaseNetworkException)
                                    Toast.makeText(context, "Network error. Please check your connection", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(context, "An error occurred, please try again later", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void handleFirebaseErrors(Exception exp) {
        if (exp instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(context, "Invalid email address", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseAuthWeakPasswordException) {
            Toast.makeText(context, "Password too weak", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(context, "User already exists", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(context, "Invalid credentials, check email and password", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseNetworkException) {
            Toast.makeText(context, "Network error. Please check your connection", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "An error occurred, please try again later", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == role_spinner.getId()){

        }
        else if(parent.getId() == comp_spinner.getId()){

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}