package com.android.rocket.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rocket.modal.Packet;
import com.android.rocket.modal.ResponseBody;
import com.android.rocket.util.Constants;
import com.android.rocket.util.CustomNotification;
import com.android.rocket.util.Client;
import com.android.rocket.util.MessageDispatcher;
import com.android.rocket.util.GlobalClass;
import com.android.rocket.modal.Message;
import com.android.rocket.R;
import com.android.rocket.util.MessageListener;
import com.android.rocket.util.TypingStatusDispatcher;
import com.android.rocket.util.TypingStatusListener;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    ImageView DismissReplyBtn;
    EditText MessageEditor;
    String ReceiverId;
    String ReceiverToken = "";
    TextView Username;
    TextView UserStatus;
    TextView ReplyPreviewText;
    String Type = "text";
    String ReplyTo = "";
    Uri mImageUri;
    RelativeLayout ReplyPreviewLayout;

    //for sending notification
    CustomNotification customNotification;
    String ReplyToOwner;

    //shared preferences
    SharedPreferences preferences;

    public static String mSelectedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (GlobalClass.mSelectedUser == null)
            finish();
        ReceiverId = GlobalClass.mSelectedUser.getId();
        preferences = getSharedPreferences("scutiPreferences", Context.MODE_PRIVATE);
        mRecyclerView = findViewById(R.id.recycler_view);
        customNotification = Client.getClient("https://fcm.googleapis.com/").create(CustomNotification.class);
        SendBtn = findViewById(R.id.SendBtn);
        ProfilePic = findViewById(R.id.profile_pic);
        SelectImageBtn = findViewById(R.id.select_image);
        SelectedImage = findViewById(R.id.selected_image);
        CloseBtn = findViewById(R.id.close_btn);
        ReplyPreviewLayout = findViewById(R.id.reply_preview_layout);
        DismissReplyBtn = findViewById(R.id.dismiss_reply_btn);
        MessageEditor = findViewById(R.id.MessageEditorET);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        Username = findViewById(R.id.username);
        ReplyPreviewText = findViewById(R.id.reply_preview_text);
        UserStatus = findViewById(R.id.user_status);
        attachListenerForReceiverToken();
        updateRecipientInfo();
        getMessages();
        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MessageEditor.getText().toString().trim().length() != 0 || mImageUri != null) {
                    sendMessage();
                } else {
                    Toast.makeText(MessageActivity.this, "Message empty", Toast.LENGTH_SHORT).show();
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

        DismissReplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Type = "text";
                ReplyPreviewLayout.setVisibility(View.GONE);
            }
        });
        //update app current state for notification generation
        preferences.edit().putString("currentlyOpenedRecipient", GlobalClass.mSelectedUser.getId()).apply();

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MessageDispatcher.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                if(message.getSenderId().equals(GlobalClass.mSelectedUser.getId())) {
                    mMessages.add(0, message);
                    setAdapter();
                }
            }
        });
        attachListenerForEditText();
    }

    private void attachListenerForReceiverToken() {
        //get token
        FirebaseFirestore.getInstance().collection("token").document(ReceiverId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.get("token") != null) {
                    ReceiverToken = documentSnapshot.get("token").toString();
                }
            }
        });
    }

    //handler to manage calls for typing status of user
    Handler mHandler = new Handler();
    Runnable runnable;
    Message TypingStatusPacket;
    String OldStatus = "";
    private void attachListenerForEditText(){
        TypingStatusPacket = new Message();
        runnable = new Runnable() {
            @Override
            public void run() {
                TypingStatusPacket.setType(Constants.TYPING_STATUS);
                TypingStatusPacket.setText("online");
                TypingStatusPacket.setSenderId(GlobalClass.LoggedInUser.getId());
                OldStatus = "online";
                sendNotification(TypingStatusPacket);
            }
        };
        MessageEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0)
                    return;
                /*if old status. earlier, was typing then,
                 no need to send packet again as its already displayed typing on the other side*/
                if(!OldStatus.equals("typing...")) {
                    Message message = new Message();
                    message.setType(Constants.TYPING_STATUS);
                    message.setText("typing...");
                    message.setSenderId(GlobalClass.LoggedInUser.getId());
                    sendNotification(message);
                    OldStatus = "typing...";
                }
                //remove callback if user is still typing
                mHandler.removeCallbacks(runnable);
                //register again
                mHandler.postDelayed(runnable, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TypingStatusDispatcher.setTypingStatusListener(new TypingStatusListener() {
            @Override
            public void onStatusReceived(final Message message) {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!UserStatus.getText().toString().contains("last") && message.getSenderId().equals(ReceiverId)){
                            UserStatus.setText(message.getText());
                        }
                    }
                });
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
                .placeholder(R.drawable.user_vector)
                .into(ProfilePic);
    }

    private void getMessages() {
        FirebaseFirestore.getInstance().collection("chat_logs/" + GlobalClass.LoggedInUser.getId() + "/" + ReceiverId)
                .orderBy("messageId", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        mMessages.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            Message message = new Message();
                            message.setMessageId(documentSnapshot.get("messageId").toString());
                            message.setReceiverId(documentSnapshot.get("receiverId").toString());
                            message.setSenderId(documentSnapshot.get("senderId").toString());
                            message.setText(documentSnapshot.get("text").toString());
                            message.setDate(documentSnapshot.get("date").toString());
                            message.setTime(documentSnapshot.get("time").toString());
                            message.setType(documentSnapshot.get("type").toString());
                            message.setPicUrl(documentSnapshot.get("picUrl").toString());
                            if (documentSnapshot.get("replyTo") != null && documentSnapshot.get("replyToOwner") != null) {
                                message.setReplyTo(documentSnapshot.get("replyTo").toString());
                                message.setReplyToOwner(documentSnapshot.get("replyToOwner").toString());
                            }
                            mMessages.add(message);
                        }
                        setAdapter();
                    }
                });
    }

    private void setAdapter() {
        MessageActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        String messageText = MessageEditor.getText().toString().trim();
        if (Type.equals("text") || Type.equals("reply")) {
            Message message = new Message(
                    "" + Type,
                    "",
                    "" + ReplyTo,
                    "" + ReplyToOwner,
                    "" + MessageId,
                    "" + GlobalClass.LoggedInUser.getName(),
                    "" + GlobalClass.LoggedInUser.getId(),
                    "" + ReceiverId,
                    "" + messageText,
                    "" + Date,
                    "" + Time);

            FirebaseFirestore.getInstance().collection("chat_logs/" + GlobalClass.LoggedInUser.getId() + "/" + ReceiverId)
                    .document(MessageId)
                    .set(message);

            FirebaseFirestore.getInstance().collection("chat_logs/" + ReceiverId + "/" + GlobalClass.LoggedInUser.getId())
                    .document(MessageId)
                    .set(message);
            sendNotification(message);
            mMessages.add(0, message);
            setAdapter();
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
                                    String text = "" + MessageEditor.getText().toString().trim();
                                    if (text.equals(""))
                                        text = "photo";
                                    Message message = new Message(
                                            "image",
                                            "" + PicUrl,
                                            "" + ReplyTo,
                                            "" + ReplyToOwner,
                                            "" + MessageId,
                                            "" + GlobalClass.LoggedInUser.getName(),
                                            "" + GlobalClass.LoggedInUser.getId(),
                                            "" + ReceiverId,
                                            "" + text,
                                            "" + Date,
                                            "" + Time);

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
                                    sendNotification(message);
                                    mMessages.add(0, message);
                                    setAdapter();
                                }
                            });
                        }
                    });
        }
        ReplyPreviewLayout.setVisibility(View.GONE);
        Type = "text";
    }

    private void sendNotification(Message message) {
        if (ReceiverToken.equals(""))
            return;
        Packet packet = new Packet(message, ReceiverToken);
        customNotification.sendNotification(packet).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    if (response.body().success == 1) {
                        System.out.println("message sent");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
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
        public void onBindViewHolder(final MessageViewHolder holder, final int position) {
            final Message message = list.get(position);
            if (message.getSenderId().equals(GlobalClass.LoggedInUser.getId())) {
                //sender or user message section
                //in sender layout,that is also, user layout, has separate layouts for different type due to alignment right problem
                holder.ReceiverMsgLayout.setVisibility(View.GONE); // hiding receiver msg layout
                holder.Time.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                String type = message.getType();
                holder.TypeImageSenderMsgLayout.setVisibility(View.GONE);
                holder.SenderReplyToText.setVisibility(View.GONE);
                holder.TypeTextReplySenderMsg.setVisibility(View.GONE);
                switch (type) {
                    case "text":
                        holder.TypeTextReplySenderMsg.setVisibility(View.VISIBLE);
                        holder.TypeTextReplySenderMsg.setText(message.getText());
                        break;
                    case "image":
                        holder.TypeImageSenderMsgLayout.setVisibility(View.VISIBLE);
                        Picasso.with(mContext)
                                .load(message.getPicUrl())
                                .placeholder(R.drawable.camera_vector)
                                .into(holder.SenderImage);
                        holder.TypeImageSenderMsg.setText(message.getText().equals("photo") ? "" : message.getText());
                        holder.SenderImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mSelectedImageUrl = message.getPicUrl();
                                startActivity(new Intent(getApplicationContext(), SelectedImage.class));
                            }
                        });
                        break;
                    case "reply":
                        holder.SenderReplyToText.setVisibility(View.VISIBLE);
                        holder.TypeTextReplySenderMsg.setVisibility(View.VISIBLE);
                        holder.TypeTextReplySenderMsg.setText(message.getText());
                        String text = "";
                        if (message.getReplyToOwner().equals(GlobalClass.LoggedInUser.getName()))
                            text = "You\n";
                        else
                            text = GlobalClass.mSelectedUser.getName() + "\n";
                        holder.SenderReplyToText.setText(text + message.getReplyTo());
                        break;
                }
            } else {
                // receiver message section
                holder.SenderMsgParentLayout.setVisibility(View.GONE);
                String type = message.getType();
                holder.ReceiverImage.setVisibility(View.GONE);
                holder.ReceiverReplyLayout.setVisibility(View.GONE);
                switch (type) {
                    case "text":
                        //do nothing, everything is already hidden at this moment
                        break;
                    case "image":
                        holder.ReceiverImage.setVisibility(View.VISIBLE);
                        Picasso.with(mContext)
                                .load(message.getPicUrl())
                                .placeholder(R.drawable.camera_vector)
                                .into(holder.ReceiverImage);
                        holder.ReceiverImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mSelectedImageUrl = message.getPicUrl();
                                startActivity(new Intent(getApplicationContext(), SelectedImage.class));
                            }
                        });
                        break;
                    case "reply":
                        holder.ReceiverReplyLayout.setVisibility(View.VISIBLE);
                        String text = "";
                        if (message.getReplyToOwner().equals(GlobalClass.LoggedInUser.getName()))
                            text = "You\n";
                        else
                            text = GlobalClass.mSelectedUser.getName() + "\n";
                        holder.ReceiverReplyText.setText(text + message.getReplyTo());
                        break;
                }
                holder.ReceiverMsg.setText(message.getText());
            }
            holder.Time.setText(message.getTime() + " " + message.getDate());
            holder.Time.setVisibility(View.GONE);
            holder.Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.Time.getVisibility() == View.VISIBLE)
                        holder.Time.setVisibility(View.GONE);
                    else
                        holder.Time.setVisibility(View.VISIBLE);
                }
            });

            holder.Item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Type = "reply";
                    String text = "";
                    if (message.getSenderId().equals(GlobalClass.LoggedInUser.getId())) {
                        text = "You\n";
                        ReplyToOwner = GlobalClass.LoggedInUser.getName();
                    } else {
                        ReplyToOwner = GlobalClass.mSelectedUser.getName();
                        text = GlobalClass.mSelectedUser.getName() + "\n";
                    }
                    text = text + message.getText();
                    ReplyTo = message.getText();
                    ReplyPreviewLayout.setVisibility(View.VISIBLE);
                    ReplyPreviewText.setText(text);
                    return true;
                }
            });
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

            //sender variables
            RelativeLayout SenderMsgParentLayout;
            RelativeLayout TypeImageSenderMsgLayout;
            ImageView SenderImage;
            TextView TypeImageSenderMsg;
            TextView TypeTextReplySenderMsg;
            TextView SenderReplyToText;

            //receiver variables
            TextView ReceiverMsg;
            RelativeLayout ReceiverMsgLayout;
            ImageView ReceiverImage;
            TextView ReceiverReplyText;
            RelativeLayout ReceiverReplyLayout;

            //common variables
            TextView Time, Seen;
            RelativeLayout Item;

            public MessageViewHolder(View itemView) {
                super(itemView);
                //sender variables initializer
                SenderMsgParentLayout = itemView.findViewById(R.id.sender_msg_parent_layout);
                TypeImageSenderMsgLayout = itemView.findViewById(R.id.type_image_sender_msg_layout);
                SenderImage = itemView.findViewById(R.id.sender_image);
                TypeImageSenderMsg = itemView.findViewById(R.id.type_image_sender_msg);
                TypeTextReplySenderMsg = itemView.findViewById(R.id.type_text_reply_sender_msg);
                SenderReplyToText = itemView.findViewById(R.id.sender_reply_to_text);

                //receiver variables initializer
                ReceiverMsg = itemView.findViewById(R.id.receiver_msg);
                ReceiverMsgLayout = itemView.findViewById(R.id.receiver_msg_layout);
                ReceiverImage = itemView.findViewById(R.id.receiver_image);
                ReceiverReplyText = itemView.findViewById(R.id.receiver_reply_to_text);
                ReceiverReplyLayout = itemView.findViewById(R.id.receiver_reply_text_layout);

                //common variables initializer
                Item = itemView.findViewById(R.id.item);
                Time = itemView.findViewById(R.id.time);
                Seen = itemView.findViewById(R.id.seen);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("destroyed");
        preferences.edit().putString("currentlyOpenedRecipient", "0").apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("paused");
        preferences.edit().putString("currentlyOpenedRecipient", "0").apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("resumed");
        preferences.edit().putString("currentlyOpenedRecipient", GlobalClass.mSelectedUser.getId()).apply();
    }
}