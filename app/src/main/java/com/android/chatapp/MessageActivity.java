package com.android.chatapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Messaging activity, opens after selecting one added friends from chat activity
 */
public class MessageActivity extends AppCompatActivity {

    MessageAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<Message> mMessages = new ArrayList<>();
    ImageView SendBtn;
    ImageView ProfilePic;
    ImageView SelectImageBtn;
    ImageView SelectedImage;
    ImageView CloseBtn;
    EditText MessageEditor;
    String ReceiverId;
    TextView Username;
    TextView UserStatus;
    String Type = "text";
    Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        mRecyclerView = findViewById(R.id.recycler_view);
        SendBtn = findViewById(R.id.SendBtn);
        ProfilePic = findViewById(R.id.profile_pic);
        SelectImageBtn = findViewById(R.id.select_image);
        SelectedImage = findViewById(R.id.selected_image);
        CloseBtn = findViewById(R.id.close_btn);
        MessageEditor = findViewById(R.id.MessageEditorET);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        Username = findViewById(R.id.username);
        UserStatus = findViewById(R.id.user_status);

        ReceiverId = GlobalClass.mSelectedUser.getId();
        getMessages();
        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MessageEditor.getText().toString().trim().length() != 0 || mImageUri != null) {
                    sendMessage();
                }
            }
        });
        SelectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MessageActivity.this);
            }
        });

        CloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageUri = null;
                Type = "text";
                SelectedImage.setVisibility(View.GONE);
                CloseBtn.setVisibility(View.GONE);
            }
        });
        updateRecipientInfo();
        setSeen();
    }

    private void setSeen() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                Type = "image";
                SelectedImage.setVisibility(View.VISIBLE);
                CloseBtn.setVisibility(View.VISIBLE);
                Picasso.with(MessageActivity.this)
                        .load(mImageUri)
                        .placeholder(R.drawable.exclamation_vector)
                        .into(SelectedImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                mImageUri = null;
                Type = "text";
                SelectedImage.setVisibility(View.GONE);
                CloseBtn.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to select image Error : " + error, Toast.LENGTH_SHORT).show();
            }
        } else {
            mImageUri = null;
            Type = "text";
            SelectedImage.setVisibility(View.GONE);
            CloseBtn.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecipientInfo() {
        Username.setText(GlobalClass.mSelectedUser.getUsername());
        FirebaseFirestore.getInstance().collection("user_status").document(ReceiverId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.get("status") != null) {
                    String LastSeen = documentSnapshot.get("status").toString();
                    if (!LastSeen.equals("online")) {
                        LastSeen = "last seen at " + LastSeen;
                    }
                    UserStatus.setText(LastSeen);
                }
            }
        });
        Picasso.with(this)
                .load(GlobalClass.mSelectedUser.getPicUrl())
                .placeholder(R.drawable.android_vector)
                .into(ProfilePic);
    }

    private void getMessages() {
        FirebaseFirestore.getInstance().collection("chat_logs/" + GlobalClass.LoggedInUser.getId() + "/" + ReceiverId)
                .orderBy("messageId", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mMessages.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            Message message = new Message();
                            message.setMessageId(documentSnapshot.get("messageId").toString());
                            message.setReceiverId(documentSnapshot.get("receiverId").toString());
                            message.setSenderId(documentSnapshot.get("senderId").toString());
                            message.setText(documentSnapshot.get("text").toString());
                            message.setDate(documentSnapshot.get("date").toString());
                            message.setTime(documentSnapshot.get("time").toString());
                            message.setSeen(documentSnapshot.get("seen").toString());
                            message.setType(documentSnapshot.get("type").toString());
                            message.setPicUrl(documentSnapshot.get("picUrl").toString());
                            mMessages.add(message);
                        }
                        mAdapter = new MessageAdapter(MessageActivity.this, mMessages);
                        mAdapter.setHasStableIds(true);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
    }

    private void sendMessage() {
        final String MessageId = System.currentTimeMillis() + "";
        DateFormat df = new SimpleDateFormat("h:mm aa");
        Date date = new Date();
        final String Time = "" + df.format(date);
        df = new SimpleDateFormat("dd/MM/yy");
        date = new Date();
        final String Date = "" + df.format(date);
        if (Type.equals("text")) {
            Message message = new Message(
                    "" + Type,
                    "",
                    "" + MessageId,
                    "" + GlobalClass.LoggedInUser.getId(),
                    "" + ReceiverId,
                    "" + MessageEditor.getText().toString().trim(),
                    "" + Date,
                    "" + Time,
                    "0");

            FirebaseFirestore.getInstance().collection("chat_logs/" + GlobalClass.LoggedInUser.getId() + "/" + ReceiverId)
                    .document(MessageId)
                    .set(message);

            FirebaseFirestore.getInstance().collection("chat_logs/" + ReceiverId + "/" + GlobalClass.LoggedInUser.getId())
                    .document(MessageId)
                    .set(message);

            MessageEditor.setText("");
        } else {
            Toast.makeText(this, "Uploading image", Toast.LENGTH_SHORT).show();
            SendBtn.setClickable(false);
            SendBtn.setAlpha(0.4f);
            SelectedImage.setVisibility(View.GONE);
            CloseBtn.setVisibility(View.GONE);
            FirebaseStorage.getInstance().getReference("sent_images").child("" + System.currentTimeMillis() + ".jpg")
                    .putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String PicUrl = uri.toString();
                                    Message message = new Message(
                                            "" + Type,
                                            "" + PicUrl,
                                            "" + MessageId,
                                            "" + GlobalClass.LoggedInUser.getId(),
                                            "" + ReceiverId,
                                            "" + MessageEditor.getText().toString().trim(),
                                            "" + Date,
                                            "" + Time,
                                            "0");

                                    FirebaseFirestore.getInstance().collection("chat_logs/" + GlobalClass.LoggedInUser.getId() + "/" + ReceiverId)
                                            .document(MessageId)
                                            .set(message);

                                    FirebaseFirestore.getInstance().collection("chat_logs/" + ReceiverId + "/" + GlobalClass.LoggedInUser.getId())
                                            .document(MessageId)
                                            .set(message);
                                    Type = "text";
                                    mImageUri = null;
                                    MessageEditor.setText("");
                                    SendBtn.setClickable(true);
                                    SendBtn.setAlpha(1.0f);
                                }
                            });
                        }
                    });
        }
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private Context mContext;
        private List<Message> list;

        public MessageAdapter(Context context, List<Message> messages) {
            mContext = context;
            list = messages;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);
            return new MessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, final int position) {
            Message message = list.get(position);
            if (message.getSenderId().equals(GlobalClass.LoggedInUser.getId())) {
                holder.ReceiverMsgLayout.setVisibility(View.GONE); // hiding receiver msg layout
                holder.Time.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                if (message.getType().equals("text")) {
                    holder.SenderMsgLayout.setVisibility(View.GONE);
                    holder.TypeTextSenderMsg.setText(message.getText());
                } else {
                    holder.TypeTextSenderMsg.setVisibility(View.GONE);
                    Picasso.with(mContext)
                            .load(message.getPicUrl())
                            .placeholder(R.drawable.camera_vector)
                            .into(holder.SenderImage);
                    holder.SenderMsg.setText(message.getText());
                }
            } else {
                holder.SenderMsgLayout.setVisibility(View.GONE); // hiding sender msg layout
                holder.TypeTextSenderMsg.setVisibility(View.GONE); // hiding sender msg layout
                if (message.getType().equals("text"))
                    holder.ReceiverImage.setVisibility(View.GONE);
                else {
                    Picasso.with(mContext)
                            .load(message.getPicUrl())
                            .placeholder(R.drawable.camera_vector)
                            .into(holder.ReceiverImage);
                }
                holder.ReceiverMsg.setText(message.getText());
            }
            holder.Time.setText(message.getTime() + " " + message.getDate());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {

            TextView SenderMsg, ReceiverMsg, Time, Seen;
            RelativeLayout SenderMsgLayout;
            RelativeLayout ReceiverMsgLayout;
            ImageView SenderImage;
            ImageView ReceiverImage;
            TextView TypeTextSenderMsg;

            public MessageViewHolder(View itemView) {
                super(itemView);
                SenderMsg = itemView.findViewById(R.id.sender_msg);
                TypeTextSenderMsg = itemView.findViewById(R.id.type_text_sender_msg);
                SenderImage = itemView.findViewById(R.id.sender_image);
                ReceiverImage = itemView.findViewById(R.id.receiver_image);
                SenderMsgLayout = itemView.findViewById(R.id.sender_msg_layout);
                ReceiverMsgLayout = itemView.findViewById(R.id.receiver_msg_layout);
                ReceiverMsg = itemView.findViewById(R.id.receiver_msg);
                Time = itemView.findViewById(R.id.time);
                Seen = itemView.findViewById(R.id.seen);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}