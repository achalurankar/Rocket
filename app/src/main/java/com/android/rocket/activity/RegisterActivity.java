package com.android.rocket.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.android.rocket.R;
import com.android.rocket.util.Client;
import com.android.rocket.util.Constants;
import com.android.rocket.util.FileUtil;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Registration activity
 * */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText mFullNameET, mEmailET, mUsernameET, mPasswordET, mConfirmPasswordET;
    Button mNextBtn;
    Button mRegisterBtn;
    TextView mLoginTV;
    TextView ResponseMsg;

    //user info variables
    String Name;
    String Username;
    String Email;
    String Password;
    String Picture;
    Uri mImageUri;
    ProgressBar mProgressBar;
    RadioButton FirstRB, SecondRB, ThirdRB;
    LinearLayout UserInfoLayout;
    RelativeLayout UserPicLayout;
    RelativeLayout UploadPhotoBtn;
    AnimatedVectorDrawableCompat animatedVectorDrawableCompat;
    AnimatedVectorDrawable animatedVectorDrawable;
    ImageView Done, ProfilePic;
    RelativeLayout RegistrationResponseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mFullNameET = findViewById(R.id.FullNameET);
        mEmailET = findViewById(R.id.EmailET);
        mUsernameET = findViewById(R.id.UsernameET);
        mPasswordET = findViewById(R.id.PasswordET);
        mConfirmPasswordET = findViewById(R.id.ConfirmPasswordET);
        mLoginTV = findViewById(R.id.login_btn);
        ResponseMsg = findViewById(R.id.response_msg);
        mNextBtn = findViewById(R.id.NextBtn);
        mRegisterBtn = findViewById(R.id.RegisterBtn);
        mProgressBar = findViewById(R.id.register_progress_line);
        FirstRB = findViewById(R.id.first_rb);
        SecondRB = findViewById(R.id.second_rb);
        ThirdRB = findViewById(R.id.third_rb);
        FirstRB.setClickable(false);
        SecondRB.setClickable(false);
        ThirdRB.setClickable(false);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        UserInfoLayout = findViewById(R.id.user_information_layout);
        RegistrationResponseLayout = findViewById(R.id.RegistrationResponseLayout);
        UserPicLayout = findViewById(R.id.user_profile_pic_layout);
        UploadPhotoBtn = findViewById(R.id.upload_pic_btn);
        Done = findViewById(R.id.done);
        ProfilePic = findViewById(R.id.ProfilePic);

        //test
        mUsernameET.setText("turntables");
        mPasswordET.setText("turntables");
        mConfirmPasswordET.setText("turntables");
        mEmailET.setText("turntables@mail.com");

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mPasswordET.getText().toString().trim().equals(mConfirmPasswordET.getText().toString().trim())
                        && !mEmailET.getText().toString().trim().equals("")
//                        && !mFullNameET.getText().toString().trim().equals("")
                        && !mUsernameET.getText().toString().trim().equals("")
                        && mPasswordET.getText().toString().trim().length() >= 6)) {
                    UserInfoLayout.setVisibility(View.GONE);
                    UserPicLayout.setVisibility(View.VISIBLE);
                    SecondRB.setChecked(true);
                    mProgressBar.setProgress(50);
                } else {
                    if (mPasswordET.getText().toString().trim().length() < 6)
                        Toast.makeText(RegisterActivity.this, "Password too weak", Toast.LENGTH_SHORT).show();
                    if (!mPasswordET.getText().toString().trim().equals(mConfirmPasswordET.getText().toString().trim()))
                        Toast.makeText(RegisterActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
                    if (mEmailET.getText().toString().trim().equals(""))
                        Toast.makeText(RegisterActivity.this, "Email Required", Toast.LENGTH_SHORT).show();
//                    if (mFullNameET.getText().toString().trim().equals(""))
//                        Toast.makeText(RegisterActivity.this, "Full Name Required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        UploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(RegisterActivity.this);
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImageUri != null) {
                    //user info variables
                    Name = mFullNameET.getText().toString().trim();
                    Email = mEmailET.getText().toString().trim();
                    Password = mPasswordET.getText().toString().trim();
                    Username = mUsernameET.getText().toString().trim().toLowerCase();

                    UserPicLayout.setVisibility(View.GONE);
                    RegistrationResponseLayout.setVisibility(View.VISIBLE);
                    registerUser();
                } else {
                    Toast.makeText(RegisterActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLoginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        System.out.println("Registration");
        final String Id = System.currentTimeMillis() + "";
        ResponseMsg.setText("Registering your info...");
        try{
            Picture = FileUtil.getBase64FromUri(mImageUri);
        } catch (IOException e) {
            Picture = null;
        }

        OkHttpClient client = new OkHttpClient.Builder().build();

        JSONObject wrapper = new JSONObject();
        try{
            wrapper.put("username", Username);
            wrapper.put("password", Password);
            wrapper.put("emailId", Email);
            wrapper.put("picture", Picture);
            wrapper.put("picture_version", System.currentTimeMillis());
        } catch (JSONException e){
            wrapper = null;
            System.out.println("Wrapper Null");
        }
        RequestBody requestBody = RequestBody.create(Client.JSON, String.valueOf(wrapper));
        Request request = new Request.Builder()
                .method("POST", requestBody)
                .url(Constants.host + "/register")
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "registerUser onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseData = response.body().string();
                    RegisterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "" + responseData, Toast.LENGTH_SHORT).show();
                            if(responseData.toLowerCase().contains("succ")){
                                System.out.println("Registration Done");
                                ResponseMsg.setText("Registration successful");
                                Drawable drawable = Done.getDrawable();
                                mProgressBar.setProgress(100);
                                ThirdRB.setChecked(true);
                                if (drawable instanceof AnimatedVectorDrawableCompat) {
                                    animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawable;
                                    animatedVectorDrawableCompat.start();
                                } else if (drawable instanceof AnimatedVectorDrawable) {
                                    animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                                    animatedVectorDrawable.start();
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finish();
                                    }
                                }, 2000);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                Picasso.with(getApplicationContext())
                        .load(mImageUri)
                        .placeholder(R.drawable.user_vector)
                        .into(ProfilePic);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Failed to select image Error : " + error , Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
        }
    }
}