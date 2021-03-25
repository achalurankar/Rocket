package com.android.chatapp.activity;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.android.chatapp.R;
import com.android.chatapp.modal.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Registration activity
 * */
public class RegisterActivity extends AppCompatActivity {

    EditText mFullNameET, mEmailET, mUsernameET, mPasswordET, mConfirmPasswordET;
    Button mNextBtn;
    Button mRegisterBtn;
    TextView mLoginTV;
    TextView ResponseMsg;
    String Name;
    String Username;
    String Email;
    String Password;
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
        UserInfoLayout = findViewById(R.id.user_information_layout);
        RegistrationResponseLayout = findViewById(R.id.RegistrationResponseLayout);
        UserPicLayout = findViewById(R.id.user_profile_pic_layout);
        UploadPhotoBtn = findViewById(R.id.upload_pic_btn);
        Done = findViewById(R.id.done);
        ProfilePic = findViewById(R.id.ProfilePic);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mPasswordET.getText().toString().trim().equals(mConfirmPasswordET.getText().toString().trim())
                        && !mEmailET.getText().toString().trim().equals("")
                        && !mFullNameET.getText().toString().trim().equals("")
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
                    if (mFullNameET.getText().toString().trim().equals(""))
                        Toast.makeText(RegisterActivity.this, "Full Name Required", Toast.LENGTH_SHORT).show();
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
                    UserPicLayout.setVisibility(View.GONE);
                    RegistrationResponseLayout.setVisibility(View.VISIBLE);
                    checkUser();
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

    private void checkUser() {
        ResponseMsg.setText("Checking username availability...");
        Name = mFullNameET.getText().toString().trim();
        Email = mEmailET.getText().toString().trim();
        Password = mPasswordET.getText().toString().trim();
        Username = mUsernameET.getText().toString().trim();
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("username", Username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean flag = true;
                        System.out.println("Username Check");
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            flag = false;
                        }
                        if (flag) {
                            registerUser();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                            SecondRB.setChecked(false);
                            mProgressBar.setProgress(0);
                            UserInfoLayout.setVisibility(View.VISIBLE);
                            UserPicLayout.setVisibility(View.GONE);
                            RegistrationResponseLayout.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void registerUser() {
        System.out.println("Registration");
        final String Id = System.currentTimeMillis() + "";
        ResponseMsg.setText("Registering your info...");
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            ResponseMsg.setText("Uploading your profile picture...");
                            FirebaseStorage.getInstance().getReference("profile_pictures/")
                                    .child(Username).putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ResponseMsg.setText("Profile picture uploaded...");
                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            ResponseMsg.setText("Finishing registration...");
                                            User user = new User(Id, Name, Username, Email, uri.toString());
                                            FirebaseFirestore.getInstance().collection("users")
                                                    .document("" + Id)
                                                    .set(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
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
                                                    });
                                        }
                                    });
                                }
                            });
                        } else {
                            UserInfoLayout.setVisibility(View.VISIBLE);
                            ResponseMsg.setVisibility(View.GONE);
                            UserPicLayout.setVisibility(View.GONE);
                            ResponseMsg.setText("");
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                        .placeholder(R.drawable.android_vector)
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