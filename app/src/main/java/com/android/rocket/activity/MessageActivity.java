package com.android.rocket.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.rocket.R;
import com.android.rocket.modal.Message;
import com.android.rocket.modal.Packet;
import com.android.rocket.modal.ResponseBody;
import com.android.rocket.modal.User;
import com.android.rocket.util.Client;
import com.android.rocket.util.Constants;
import com.android.rocket.util.CustomNotification;
import com.android.rocket.util.MessageDispatcher;
import com.android.rocket.util.Session;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    Uri mImageUri;
    RelativeLayout ReplyPreviewLayout;

    OkHttpClient mClient;
    //for sending notification
    CustomNotification customNotification;

    //shared preferences
    SharedPreferences preferences;

    public static String mSelectedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (Session.SelectedUser == null)
            finish();
        mClient = new OkHttpClient.Builder().build();
        ReceiverId = Session.SelectedUser.getId();
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

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        Username.setText(Session.SelectedUser.getUsername());
        Picasso.with(this)
                .load(Session.SelectedUser.getPicture())
                .placeholder(R.drawable.user_vector)
                .into(ProfilePic);
    }

    private void getMessages() {
        final Request request = new Request.Builder()
                .url(Constants.host + "/message/" + Session.LoggedInUser.getUserId() + "/" + Session.SelectedUser.getUserId())
                .build();
        mClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseData = response.body().string();
                    try {
                        JSONArray array = new JSONArray(responseData);
                        int length = array.length();
                        mMessages.clear();
                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject = (JSONObject) array.get(i);
                            Message message = new Message(
                                    jsonObject.getString("messageId"),
                                    jsonObject.getString("conversationId"),
                                    jsonObject.getInt("senderId"),
                                    jsonObject.getInt("receiverId"),
                                    jsonObject.getString("picture"),
                                    jsonObject.getString("text"),
                                    jsonObject.getString("type"),
                                    jsonObject.getBoolean("seen"),
                                    jsonObject.getString("dateSent"),
                                    jsonObject.getString("dateUpdated"));
                            mMessages.add(message);
                        }
                        setAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
        String messageText = MessageEditor.getText().toString().trim();
        if (Type.equals("text")
//                || Type.equals("reply")
        ) {
            Message message = new Message();
            JSONObject wrapper = new JSONObject();
            try {
                wrapper.put("senderId", Session.LoggedInUser.getUserId());
                wrapper.put("receiverId", Session.SelectedUser.getUserId());
                wrapper.put("picture", "no-pic");
                wrapper.put("text", messageText);
                wrapper.put("type", Type);
            } catch (JSONException e) {
                wrapper = null;
                e.printStackTrace();
            }
            RequestBody requestBody = RequestBody.create(Client.JSON, String.valueOf(wrapper));
            final Request request = new Request.Builder()
                    .method("POST", requestBody)
                    .url(Constants.host + "/message")
                    .addHeader("Content-Type", "application/json")
                    .build();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mClient.newCall(request).execute();
                        MessageActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MessageEditor.setText("");
                                getMessages();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        //if user wants to send image
        } else {
            if (true)
                return;
            Toast.makeText(this, "Uploading image", Toast.LENGTH_SHORT).show();
            SendBtn.setClickable(false);
            SendBtn.setAlpha(0.4f);
            SelectedImage.setVisibility(View.GONE);
            CloseBtn.setVisibility(View.GONE);
//            String PicUrl = uri.toString();
            String text = "" + MessageEditor.getText().toString().trim();
            if (text.equals(""))
                text = "photo";
            Message message = new Message();
            Type = "text";
//            mImageUri = null;
            MessageEditor.setText("");
            SendBtn.setClickable(true);
            SendBtn.setAlpha(1.0f);
//            sendNotification(message);
            mMessages.add(0, message);
            setAdapter();
        }
        ReplyPreviewLayout.setVisibility(View.GONE);
        Type = "text";
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
            if (message.getSenderId() == (Session.LoggedInUser.getUserId())) {
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
                                .load(message.getPicture())
                                .placeholder(R.drawable.camera_vector)
                                .into(holder.SenderImage);
                        holder.TypeImageSenderMsg.setText(message.getText().equals("photo") ? "" : message.getText());
                        holder.SenderImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mSelectedImageUrl = message.getPicture();
                                startActivity(new Intent(getApplicationContext(), SelectedImage.class));
                            }
                        });
                        break;
                    case "reply":
//                        holder.SenderReplyToText.setVisibility(View.VISIBLE);
//                        holder.TypeTextReplySenderMsg.setVisibility(View.VISIBLE);
//                        holder.TypeTextReplySenderMsg.setText(message.getText());
//                        String text = "";
//                        if (message.getReplyToOwner().equals(Session.LoggedInUser.getName()))
//                            text = "You\n";
//                        else
//                            text = Session.mSelectedUser.getName() + "\n";
//                        holder.SenderReplyToText.setText(text + message.getReplyTo());
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
                                .load(message.getPicture())
                                .placeholder(R.drawable.camera_vector)
                                .into(holder.ReceiverImage);
                        holder.ReceiverImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mSelectedImageUrl = message.getPicture();
                                startActivity(new Intent(getApplicationContext(), SelectedImage.class));
                            }
                        });
                        break;
                    case "reply":
//                        holder.ReceiverReplyLayout.setVisibility(View.VISIBLE);
//                        String text = "";
//                        if (message.getReplyToOwner().equals(Session.LoggedInUser.getName()))
//                            text = "You\n";
//                        else
//                            text = Session.mSelectedUser.getName() + "\n";
//                        holder.ReceiverReplyText.setText(text + message.getReplyTo());
//                        break;
                }
                holder.ReceiverMsg.setText(message.getText());
            }
            holder.Time.setText(message.getDateSent());
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
//                    Type = "reply";
//                    String text = "";
//                    if (message.getSenderId().equals(Session.LoggedInUser.getId())) {
//                        text = "You\n";
//                        ReplyToOwner = Session.LoggedInUser.getName();
//                    } else {
//                        ReplyToOwner = Session.mSelectedUser.getName();
//                        text = Session.mSelectedUser.getName() + "\n";
//                    }
//                    text = text + message.getText();
//                    ReplyTo = message.getText();
//                    ReplyPreviewLayout.setVisibility(View.VISIBLE);
//                    ReplyPreviewText.setText(text);
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
        preferences.edit().putString("currentlyOpenedRecipient", Session.SelectedUser.getId()).apply();
    }
}