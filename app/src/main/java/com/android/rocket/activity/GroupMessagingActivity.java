package com.android.rocket.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.rocket.util.Session;
import com.android.rocket.modal.Message;
import com.android.rocket.R;
import com.android.rocket.modal.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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

public class GroupMessagingActivity extends AppCompatActivity {

    GroupMessageAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<Message> mMessages = new ArrayList<>();
    ImageView SendBtn, Options;
    ImageView GroupIcon;
    EditText MessageEditor;
    ImageView SelectImageBtn;
    ImageView SelectedImage;
    ImageView CloseBtn;
    String ReceiverId;
    TextView GroupName;
    TextView UserStatus;
    Dialog mDialog;
    String Type = "text";
    Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messaging);
        mRecyclerView = findViewById(R.id.recycler_view);
        SendBtn = findViewById(R.id.SendBtn);
        Options = findViewById(R.id.options);
        mDialog = new Dialog(this);
        GroupIcon = findViewById(R.id.group_icon);
        SelectImageBtn = findViewById(R.id.select_image);
        SelectedImage = findViewById(R.id.selected_image);
        CloseBtn = findViewById(R.id.close_btn);
        MessageEditor = findViewById(R.id.MessageEditorET);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        GroupName = findViewById(R.id.name);
        UserStatus = findViewById(R.id.user_status);
        updateGroupInfo();
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
                        .start(GroupMessagingActivity.this);
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

        Options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupAddParticipantsDialog();
            }
        });
        getMessages();
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
                Picasso.with(GroupMessagingActivity.this)
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

    public void getMessages() {
        FirebaseFirestore.getInstance().collection("groups/" + Session.SelectedGroup.getGroupId() + "/messages")
                .orderBy("messageId", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mMessages.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            Message message = new Message();
//                            message.setMessageId(documentSnapshot.get("messageId").toString());
//                            message.setSenderName(documentSnapshot.get("senderName").toString());
//                            message.setSenderId(documentSnapshot.get("senderId").toString());
//                            message.setSenderPicUrl(documentSnapshot.get("senderPicUrl").toString());
//                            message.setPicUrl(documentSnapshot.get("picUrl").toString());
//                            message.setText(documentSnapshot.get("text").toString());
//                            message.setType(documentSnapshot.get("type").toString());
//                            message.setDate(documentSnapshot.get("date").toString());
//                            message.setTime(documentSnapshot.get("time").toString());
                            mMessages.add(message);
                        }
                        mAdapter = new GroupMessageAdapter(GroupMessagingActivity.this, mMessages);
                        mAdapter.setHasStableIds(true);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
    }

    public void sendMessage() {
        final String MessageId = System.currentTimeMillis() + "";
        DateFormat df = new SimpleDateFormat("h:mm aa");
        Date date = new Date();
        String Time = "" + df.format(date);
        df = new SimpleDateFormat("dd/MM/yy");
        date = new Date();
        String Date = "" + df.format(date);
        final Message message = new Message(
//                "" + Type,
//                "",
//                "" + MessageId,
//                "" + Session.mSelectedGroup.getGroupId(),
//                "" + Session.LoggedInUser.getName(),
//                "" + Session.LoggedInUser.getId(),
//                "" + Session.LoggedInUser.getPicUrl(),
//                "" + Date,
//                "" + Time,
//                "" + MessageEditor.getText().toString().trim()
        );

        if (Type.equals("text")) {
            FirebaseFirestore.getInstance().collection("groups/" + Session.SelectedGroup.getGroupId() + "/messages")
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
//                                    message.setPicUrl(PicUrl);
                                    FirebaseFirestore.getInstance().collection("groups/" + Session.SelectedGroup.getGroupId() + "/messages")
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

    public void updateGroupInfo() {
        GroupName.setText(Session.SelectedGroup.getGroupName());
        if (Session.SelectedGroup.getPicUrl() != null)
            Picasso.with(this)
                    .load(Session.SelectedGroup.getPicUrl())
                    .placeholder(R.drawable.group_vector)
                    .into(GroupIcon);
    }

    public void setupAddParticipantsDialog() {
        final List<User> users = new ArrayList<>();
        mDialog.setContentView(R.layout.dialog_add_participants);
        Window window = mDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        RecyclerView recyclerView = mDialog.findViewById(R.id.recycler_view);
        ImageView close = mDialog.findViewById(R.id.close_dialog);
        final TextView loading = mDialog.findViewById(R.id.loading);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        final UserAdapter adapter = new UserAdapter(GroupMessagingActivity.this, users);
        recyclerView.setAdapter(adapter);
        FirebaseFirestore.getInstance().collection("users/" + Session.LoggedInUser.getUserId() + "/friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                users.clear();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    FirebaseFirestore.getInstance().collection("users")
                            .document(documentSnapshot.get("id").toString()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = new User();
                                    System.out.println("Into User");
//                                    user.setId(documentSnapshot.get("id").toString());
//                                    user.setName(documentSnapshot.get("name").toString());
//                                    user.setUsername(documentSnapshot.get("username").toString());
//                                    user.setEmail(documentSnapshot.get("email").toString());
//                                    user.setPicUrl(documentSnapshot.get("picUrl").toString());
                                    users.add(user);
                                    adapter.notifyDataSetChanged();
                                    loading.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });
        mDialog.show();
    }

    public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

        Context mContext;
        List<Message> mList;

        GroupMessageAdapter(Context context, List<Message> list) {
            mContext = context;
            this.mList = list;
        }

        @NonNull
        @Override
        public GroupMessageAdapter.GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.group_message_item, parent, false);
            return new GroupMessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupMessageAdapter.GroupMessageViewHolder holder, final int position) {
//            Message message = mList.get(position);
//            if (message.getSenderId().equals(Session.LoggedInUser.getId())) {
//                holder.ReceiverMsgLayout.setVisibility(View.GONE);
//                holder.ImageParentLayout.setVisibility(View.GONE);
//                holder.Time.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
//                if (message.getType().equals("text")) {
//                    holder.SenderMsgLayout.setVisibility(View.GONE);
//                    holder.TypeTextSenderMsg.setText(message.getText());
//                } else {
//                    holder.TypeTextSenderMsg.setVisibility(View.GONE);
//                    Picasso.with(mContext)
//                            .load(message.getPicUrl())
//                            .placeholder(R.drawable.camera_vector)
//                            .into(holder.SenderImage);
//                    holder.SenderMessage.setText(message.getText());
//                }
//            } else {
//                holder.SenderMsgLayout.setVisibility(View.GONE);
//                holder.TypeTextSenderMsg.setVisibility(View.GONE);
//                if (message.getType().equals("text"))
//                    holder.ReceiverImage.setVisibility(View.GONE);
//                else {
//                    Picasso.with(mContext)
//                            .load(message.getPicUrl())
//                            .placeholder(R.drawable.camera_vector)
//                            .into(holder.ReceiverImage);
//                }
//                holder.ReceiverMessage.setText(message.getText());
//                if ((position + 1 != mList.size()
//                        && !mList.get(position + 1).getSenderId().equals(message.getSenderId()))
//                ||
//                    (position == mList.size() - 1 && !message.getSenderId().equals(Session.LoggedInUser.getId()))) {
//                    Picasso.with(mContext)
//                            .load(message.getSenderPicUrl())
//                            .placeholder(R.drawable.user_vector)
//                            .into(holder.ProfilePic);
//                    holder.SenderName.setText(message.getSenderName());
//                } else {
//                    holder.ImageParentLayout.setVisibility(View.INVISIBLE);
//                    holder.SenderName.setVisibility(View.GONE);
//
//                }
//                holder.ReceiverMessage.setText(message.getText());
//            }
//            holder.Time.setText(message.getTime() + " " + message.getDate());
        }

        @Override
        public int getItemCount() {
            return this.mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class GroupMessageViewHolder extends RecyclerView.ViewHolder {

            TextView SenderName, ReceiverMessage, SenderMessage, Time;
            RelativeLayout ReceiverMsgLayout, ImageParentLayout;
            ImageView ProfilePic;
            RelativeLayout SenderMsgLayout;
            ImageView SenderImage;
            ImageView ReceiverImage;
            TextView TypeTextSenderMsg;

            public GroupMessageViewHolder(View itemView) {
                super(itemView);
                ReceiverMsgLayout = itemView.findViewById(R.id.receiver_msg_layout);
                ImageParentLayout = itemView.findViewById(R.id.ImageParentLayout);
                SenderName = itemView.findViewById(R.id.sender_name);
                SenderImage = itemView.findViewById(R.id.sender_image);
                ReceiverImage = itemView.findViewById(R.id.receiver_image);
                SenderMsgLayout = itemView.findViewById(R.id.type_image_sender_msg_layout);
                TypeTextSenderMsg = itemView.findViewById(R.id.type_text_reply_sender_msg);
                ReceiverMessage = itemView.findViewById(R.id.receiver_msg);
                SenderMessage = itemView.findViewById(R.id.type_image_sender_msg);
                Time = itemView.findViewById(R.id.time);
                ProfilePic = itemView.findViewById(R.id.profile_pic);
            }
        }
    }

    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ImageViewHolder> {
        private Context mContext;
        private List<User> list;

        public UserAdapter(Context context, List<User> users) {
            mContext = context;
            list = users;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.user_info_item, parent, false);
            return new ImageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, final int position) {

//            User user = list.get(position);
//            holder.Name.setText(user.getName());
//            holder.Username.setText(user.getUsername());
//            holder.AddFriend.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String id = list.get(position).getId();
//                    String name = list.get(position).getUsername();
//                    addUser(id, name);
//                }
//            });
//            Picasso.with(mContext)
//                    .load(user.getPicUrl())
//                    .placeholder(R.drawable.android_vector)
//                    .into(holder.Icon);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView Icon;
            TextView Name, Username;
            RelativeLayout AddFriend;

            public ImageViewHolder(View itemView) {
                super(itemView);
                Icon = itemView.findViewById(R.id.icon);
                Name = itemView.findViewById(R.id.name);
                Username = itemView.findViewById(R.id.username);
                AddFriend = itemView.findViewById(R.id.AddFriend);
            }
        }
    }

    private void addUser(String id, final String name) {
        String groupId = Session.SelectedGroup.getGroupId();
        FirebaseFirestore.getInstance().collection("users/" + id + "/groups")
                .document(groupId)
                .set(Session.SelectedGroup)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupMessagingActivity.this, "" + name + " added to group", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}