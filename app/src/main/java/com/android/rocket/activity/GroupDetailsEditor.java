package com.android.rocket.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rocket.util.Session;
import com.android.rocket.modal.GroupInfo;
import com.android.rocket.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class GroupDetailsEditor extends AppCompatActivity {

    EditText GroupName;
    Button Add;
    ImageView GroupIcon;
    Uri mImageUri;
    TextView ProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details_editor);
        GroupName = findViewById(R.id.group_name);
        Add = findViewById(R.id.add);
        GroupIcon = findViewById(R.id.group_icon);
        ProgressText = findViewById(R.id.progress_msg);
        GroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0 && mImageUri != null) {
                    Add.setAlpha(1);
                    Add.setClickable(true);
                } else {
                    Add.setAlpha(0.4f);
                    Add.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGroup();
                Add.setClickable(false);
            }
        });

        GroupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(GroupDetailsEditor.this);
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
                Picasso.with(GroupDetailsEditor.this)
                        .load(mImageUri)
                        .placeholder(R.drawable.exclamation_vector)
                        .into(GroupIcon);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Failed to select image Error : " + error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
        }
        if (GroupName.getText().toString().trim().length() != 0 && mImageUri != null) {
            Add.setAlpha(1);
            Add.setClickable(true);
        } else {
            Add.setAlpha(0.4f);
            Add.setClickable(false);
        }
    }

    public void addGroup() {
        final String Id = System.currentTimeMillis() + "";
        ProgressText.setText("Uploading group icon...");
        FirebaseStorage.getInstance().getReference("group_icons").child(Id).putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String PicUrl = uri.toString();
                                ProgressText.setText("Finishing...");
                                String GroupNameStr = GroupName.getText().toString().trim();
                                GroupInfo groupInfo = new GroupInfo("" + Id, "" + GroupNameStr, "" + PicUrl);
                                FirebaseFirestore.getInstance().collection("groups").document(Id).set(groupInfo);
                                FirebaseFirestore.getInstance().collection("users/" + Session.LoggedInUser.getId() + "/groups").document(Id).set(groupInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                });
    }
}