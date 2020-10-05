package com.android.chatapp;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

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
    String ReceiverId;
    TextView GroupName;
    TextView UserStatus;
    Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messaging);
        mRecyclerView = findViewById(R.id.recycler_view);
        SendBtn = findViewById(R.id.SendBtn);
        Options = findViewById(R.id.options);
        mDialog = new Dialog(this);
        GroupIcon = findViewById(R.id.group_icon);
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
                if (MessageEditor.getText().toString().trim().length() != 0) {
                    sendMessage();
                }
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

    public void getMessages() {
        FirebaseFirestore.getInstance().collection("groups/" + GlobalClass.mSelectedGroup.getGroupId() + "/messages")
                .orderBy("messageId", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mMessages.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            Message message = new Message();
                            message.setMessageId(documentSnapshot.get("messageId").toString());
                            message.setSenderName(documentSnapshot.get("senderName").toString());
                            message.setSenderId(documentSnapshot.get("senderId").toString());
                            message.setSenderPicUrl(documentSnapshot.get("senderPicUrl").toString());
                            message.setText(documentSnapshot.get("text").toString());
                            message.setDate(documentSnapshot.get("date").toString());
                            message.setTime(documentSnapshot.get("time").toString());
                            mMessages.add(message);
                        }
                        mAdapter = new GroupMessageAdapter(GroupMessagingActivity.this, mMessages);
                        mAdapter.setHasStableIds(true);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
    }

    public void sendMessage() {
        String MessageId = System.currentTimeMillis() + "";
        DateFormat df = new SimpleDateFormat("h:mm aa");
        Date date = new Date();
        String Time = "" + df.format(date);
        df = new SimpleDateFormat("dd/MM/yy");
        date = new Date();
        String Date = "" + df.format(date);
        Message message = new Message(
                "" + MessageId,
                "" + GlobalClass.mSelectedGroup.getGroupId(),
                "" + GlobalClass.LoggedInUser.getName(),
                "" + GlobalClass.LoggedInUser.getId(),
                "" + GlobalClass.LoggedInUser.getPicUrl(),
                "" + Date,
                "" + Time,
                "" + MessageEditor.getText().toString().trim());

        FirebaseFirestore.getInstance().collection("groups/" + GlobalClass.mSelectedGroup.getGroupId() + "/messages")
                .document(MessageId)
                .set(message);

        MessageEditor.setText("");
    }

    public void updateGroupInfo() {
        GroupName.setText(GlobalClass.mSelectedGroup.getGroupName());
        if (GlobalClass.mSelectedGroup.getPicUrl() != null)
            Picasso.with(this)
                    .load(GlobalClass.mSelectedGroup.getPicUrl())
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
        FirebaseFirestore.getInstance().collection("users/" + GlobalClass.LoggedInUser.getId() + "/friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                                    user.setId(documentSnapshot.get("id").toString());
                                    user.setName(documentSnapshot.get("name").toString());
                                    user.setUsername(documentSnapshot.get("username").toString());
                                    user.setEmail(documentSnapshot.get("email").toString());
                                    user.setPicUrl(documentSnapshot.get("picUrl").toString());
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
        public void onBindViewHolder(@NonNull GroupMessageAdapter.GroupMessageViewHolder holder, int position) {
            Message message = mList.get(position);
            if (message.getSenderId().equals(GlobalClass.LoggedInUser.getId())) {
                holder.ReceiverMsgLayout.setVisibility(View.GONE);
                holder.ImageParentLayout.setVisibility(View.GONE);
                holder.SenderMessage.setText(message.getText());
                holder.Time.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            } else {
                holder.SenderMessage.setVisibility(View.GONE);
                Picasso.with(mContext)
                        .load(message.getSenderPicUrl())
                        .placeholder(R.drawable.user_vector)
                        .into(holder.ProfilePic);
                holder.ReceiverMessage.setText(message.getText());
                holder.SenderName.setText(message.getSenderName());
            }
            holder.Time.setText(message.getTime() + " " + message.getDate());
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

            public GroupMessageViewHolder(View itemView) {
                super(itemView);
                ReceiverMsgLayout = itemView.findViewById(R.id.receiver_msg_layout);
                ImageParentLayout = itemView.findViewById(R.id.ImageParentLayout);
                SenderName = itemView.findViewById(R.id.sender_name);
                ReceiverMessage = itemView.findViewById(R.id.receiver_msg);
                SenderMessage = itemView.findViewById(R.id.sender_msg);
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

            User user = list.get(position);
            holder.Name.setText(user.getName());
            holder.Username.setText(user.getUsername());
            holder.AddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = list.get(position).getId();
                    String name = list.get(position).getUsername();
                    addUser(id, name);
                }
            });
            Picasso.with(mContext)
                    .load(user.getPicUrl())
                    .placeholder(R.drawable.android_vector)
                    .into(holder.Icon);
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
        String groupId = GlobalClass.mSelectedGroup.getGroupId();
        FirebaseFirestore.getInstance().collection("users/" + id + "/groups")
                .document(groupId)
                .set(GlobalClass.mSelectedGroup)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupMessagingActivity.this, "" + name + " added to group", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}