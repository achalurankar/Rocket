package com.android.rocket.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rocket.util.GlobalClass;
import com.android.rocket.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Add friends activity, left to home screen
 */

public class AddFriendsActivity extends AppCompatActivity {

    EditText SearchBarET;
    TextView RightNavText;
    LinearLayout RightNavBtn;
    Button AddBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        SearchBarET = findViewById(R.id.SearchBarET);
        AddBtn = findViewById(R.id.add_btn);
        SearchBarET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AddBtn.setAlpha(0.45f);
                AddBtn.setClickable(false);
                if (s.toString().trim().equals("")) {
                } else {
                    AddBtn.setAlpha(1);
                    AddBtn.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SearchBarET.getText().toString().trim().equals("")) {
                    String username = SearchBarET.getText().toString().trim().toLowerCase();
                    addUser(username);
                }
            }
        });
        setupNavigation();
    }

    private void addUser(String username) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean flag = true;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String id = documentSnapshot.get("id").toString();
                            String currentUserId = GlobalClass.LoggedInUser.getId();
                            Map<String, String> map = new HashMap<>();
                            map.put("id", currentUserId);
                            map.put("accepted", "0"); // not in use currently
                            FirebaseFirestore.getInstance().collection("users/" + id + "/requests")
                                    .document(currentUserId)
                                    .set(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddFriendsActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            flag = false;
                            break;
                        }
                        if(flag) {
                            Toast.makeText(AddFriendsActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupNavigation() {
        RightNavText = findViewById(R.id.right_nav_text);
        RightNavText.setPaintFlags(RightNavText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RightNavBtn = findViewById(R.id.RightNavBtn);

        RightNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RootChatsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), RootChatsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}