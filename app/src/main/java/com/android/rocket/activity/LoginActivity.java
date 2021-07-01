package com.android.rocket.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.rocket.R;
import com.android.rocket.util.Constants;
import com.android.rocket.util.Session;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Login activity
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    EditText mUsernameET, mPasswordET;
    TextView mSignUpTV;
    Button mLoginBtn;

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
                    String Password = mPasswordET.getText().toString().trim();
                    loginUser(Username, Password);
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

    private void loginUser(String username, String password) {

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(Constants.host + "/login/" + username + "/" + password)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String responseData = response.body().string();
                if (response.code() == 404) {
                    //invalid login
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, responseData, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (response.isSuccessful()) {
                    //initialize session variables
                    if (Session.saveUserInfo(responseData)) {
                        //save current user info in device
                        getSharedPreferences(Constants.ROCKET_PREFERENCES, Context.MODE_PRIVATE)
                                .edit()
                                .putString(Constants.USER_INFO_JSON, responseData)
                                .apply();
                        //start chats activity
                        startActivity(new Intent(LoginActivity.this, RootChatsActivity.class));
                        finish();
                    } else {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "onResponse: " + responseData);
                }
            }
        });
    }

    private void setupActivityVariables() {
        mUsernameET = findViewById(R.id.UsernameET);
        mPasswordET = findViewById(R.id.PasswordET);
        mSignUpTV = findViewById(R.id.sign_up_btn);
        mLoginBtn = findViewById(R.id.LoginBtn);
    }
}