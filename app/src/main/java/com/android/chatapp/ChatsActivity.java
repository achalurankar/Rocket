package com.android.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Home screen activity
 */

public class ChatsActivity extends AppCompatActivity {

    ChatsAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<User> mChats = new ArrayList<>();
    static User mSelectedUser;
    TextView LeftNavText;
    TextView RightNavText;
    LinearLayout LeftNavBtn;
    LinearLayout RightNavBtn;
    TextView CurrentUsername;
    TextView Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        setupNavigation();
        CurrentUsername = findViewById(R.id.currentUsername);
        CurrentUsername.setPaintFlags(CurrentUsername.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        CurrentUsername.setText(GlobalClass.LoggedInUser.getUsername());
        Logout = findViewById(R.id.logout);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat df = new SimpleDateFormat("h:mm aa dd/MM/yy");
                Date obj = new Date();
                System.out.println("Last Online : " + df.format(obj));
                final Map<String, String> map = new HashMap<>();
                map.put("status", "" + df.format(obj));
                FirebaseFirestore.getInstance().collection("user_status")
                        .document(GlobalClass.LoggedInUser.getId())
                        .set(map);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finishAffinity();
                GlobalClass.LoggedInUser = new User();
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ChatsAdapter(this, mChats);
        mRecyclerView.setAdapter(mAdapter);
        updateChats();
    }

    //method to update chats
    private void updateChats() {
        FirebaseFirestore.getInstance().collection("users/" + GlobalClass.LoggedInUser.getId() + "/friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mChats.clear();
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
                                    mChats.add(user);
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                }
            }
        });
    }

    //adapter class
    public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
        private Context mContext;
        private List<User> list;

        public ChatsAdapter(Context context, List<User> users) {
            mContext = context;
            list = users;
        }

        @Override
        public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chats_item, parent, false);
            return new ChatsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ChatsViewHolder holder, final int position) {

            User user = list.get(position);
            String from = user.getName() + " (@ " + user.getUsername() + ")";
            holder.From.setText(from);
            holder.MessageSubject.setText("Start new chat");
            holder.Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelectedUser = list.get(position);
                    startActivity(new Intent(ChatsActivity.this, MessageActivity.class));
                    overridePendingTransition(0, 0);
                }
            });

            Picasso.with(ChatsActivity.this)
                    .load(user.getPicUrl())
                    .placeholder(R.drawable.android_vector)
                    .into(holder.Icon);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ChatsViewHolder extends RecyclerView.ViewHolder {

            ImageView Icon;
            TextView From, MessageSubject, Time, UnreadCount;
            RelativeLayout Item;

            public ChatsViewHolder(View itemView) {
                super(itemView);
                Item = itemView.findViewById(R.id.item);
                Icon = itemView.findViewById(R.id.icon);
                From = itemView.findViewById(R.id.name);
                MessageSubject = itemView.findViewById(R.id.username);
                Time = itemView.findViewById(R.id.time);
                UnreadCount = itemView.findViewById(R.id.unread_count);
            }
        }
    }

    //method to set up navigation
    private void setupNavigation() {
        LeftNavText = findViewById(R.id.left_nav_text);
        RightNavText = findViewById(R.id.right_nav_text);
        LeftNavText.setPaintFlags(LeftNavText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RightNavText.setPaintFlags(RightNavText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        LeftNavBtn = findViewById(R.id.LeftNavBtn);
        RightNavBtn = findViewById(R.id.RightNavBtn);
        LeftNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddFriendsActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
        RightNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FriendRequestsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }
}