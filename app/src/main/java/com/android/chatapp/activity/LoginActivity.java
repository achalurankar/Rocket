package com.android.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chatapp.util.GlobalClass;
import com.android.chatapp.R;
import com.android.chatapp.util.TokenRefresher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Login activity
 * */

public class LoginActivity extends AppCompatActivity {

    EditText mUsernameET, mPasswordET;
    TextView mSignUpTV;
    Button mLoginBtn;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupActivityVariables();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUsernameET.getText().toString().trim().equals("") && !mPasswordET.getText().toString().trim().equals("")) {
                    String Username = mUsernameET.getText().toString().trim();
                    FirebaseFirestore.getInstance().collection("users")
                            .whereEqualTo("username", Username)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    boolean flag = true;
                                    String Email;
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        Email = documentSnapshot.get("email").toString();
                                        flag = false;
                                        loginUser(Email);
                                    }
                                    if (flag) {
                                        Toast.makeText(LoginActivity.this, "user does not exist", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else
                    Toast.makeText(LoginActivity.this, "Some fields are empty", Toast.LENGTH_SHORT).show();
            }
        });

        mSignUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    private void loginUser(String Email) {
        String password;
        password = mPasswordET.getText().toString().trim();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(Email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth = FirebaseAuth.getInstance();
                            mUser = mAuth.getCurrentUser();
                            if (true || mUser.isEmailVerified()) {
                                String Email = mUser.getEmail();
                                FirebaseFirestore.getInstance().collection("users")
                                        .whereEqualTo("email", Email)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                                    GlobalClass.LoggedInUser.setName(documentSnapshot.get("name").toString());
                                                    GlobalClass.LoggedInUser.setUsername(documentSnapshot.get("username").toString());
                                                    GlobalClass.LoggedInUser.setId(documentSnapshot.get("id").toString());
                                                    GlobalClass.LoggedInUser.setEmail(documentSnapshot.get("email").toString());
                                                    GlobalClass.LoggedInUser.setPicUrl(documentSnapshot.get("picUrl").toString());
                                                }
                                                final Map<String, String> map = new HashMap<>();
                                                map.put("status", "online");
                                                FirebaseFirestore.getInstance().collection("user_status")
                                                        .document(GlobalClass.LoggedInUser.getId())
                                                        .set(map);
                                                TokenRefresher.updateToken(GlobalClass.LoggedInUser.getId());
                                                startActivity(new Intent(LoginActivity.this, RootChatsActivity.class));
                                                finish();
                                            }
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your account", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                        }
                    }
                });
    }

    private void setupActivityVariables() {
        mUsernameET = findViewById(R.id.UsernameET);
        mPasswordET = findViewById(R.id.PasswordET);
        mSignUpTV = findViewById(R.id.sign_up_btn);
        mLoginBtn = findViewById(R.id.LoginBtn);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }
}