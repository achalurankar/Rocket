package com.android.chatapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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

/**
 *  Messaging activity, opens after selecting one added friends from chat activity
 * */
public class MessageActivity extends AppCompatActivity {

    MessageAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<Message> mMessages = new ArrayList<>();
    ImageView SendBtn;
    ImageView ProfilePic;
    EditText MessageEditor;
    String ReceiverId;
    TextView Username;
    TextView UserStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        mRecyclerView = findViewById(R.id.recycler_view);
        SendBtn = findViewById(R.id.SendBtn);
        ProfilePic = findViewById(R.id.profile_pic);
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
                if (MessageEditor.getText().toString().trim().length() != 0) {
                    sendMessage();
                }
            }
        });
        updateRecipientInfo();
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
                            mMessages.add(message);
                        }
                        mAdapter = new MessageAdapter(MessageActivity.this, mMessages);
                        mAdapter.setHasStableIds(true);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
    }

    private void sendMessage() {
        String MessageId = System.currentTimeMillis() + "";

        DateFormat df = new SimpleDateFormat("h:mm aa");
        Date date = new Date();
        String Time = "" + df.format(date);
        df = new SimpleDateFormat("dd/MM/yy");
        date = new Date();
        String Date = "" + df.format(date);
        Message message = new Message("" + MessageId,
                "" + GlobalClass.LoggedInUser.getId(),
                "" + ReceiverId,
                "" + MessageEditor.getText().toString().trim(),
                "" + Date,
                "" + Time);

        FirebaseFirestore.getInstance().collection("chat_logs/" + GlobalClass.LoggedInUser.getId() + "/" + ReceiverId)
                .document(MessageId)
                .set(message);

        FirebaseFirestore.getInstance().collection("chat_logs/" + ReceiverId + "/" + GlobalClass.LoggedInUser.getId())
                .document(MessageId)
                .set(message);

        MessageEditor.setText("");
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
                holder.ReceiverMsg.setVisibility(View.GONE);
                holder.SenderMsg.setText(message.getText());
                holder.Time.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            } else {
                holder.SenderMsg.setVisibility(View.GONE);
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

            TextView SenderMsg, ReceiverMsg, Time;

            public MessageViewHolder(View itemView) {
                super(itemView);
                SenderMsg = itemView.findViewById(R.id.sender_msg);
                ReceiverMsg = itemView.findViewById(R.id.receiver_msg);
                Time = itemView.findViewById(R.id.time);
            }
        }
    }
}