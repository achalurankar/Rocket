package com.android.chatapp.activity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.android.chatapp.util.GlobalClass;
import com.android.chatapp.R;
import com.android.chatapp.modal.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Friend requests activity right to home screen
 * */
public class FriendRequestsActivity extends AppCompatActivity {

    RequestAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<User> mRequests = new ArrayList<>();
    TextView LeftNavText;
    LinearLayout LeftNavBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RequestAdapter(this, mRequests);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        getRequests();
        setupNavigation();
    }

    private void getRequests() {
        FirebaseFirestore.getInstance().collection("users/" + GlobalClass.LoggedInUser.getId() + "/requests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mRequests.clear();
                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                    String requestID = snapshot.get("id").toString();
                    FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", requestID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                User user = new User();
                                user.setId(snapshot.get("id").toString());
                                user.setName(snapshot.get("name").toString());
                                user.setEmail(snapshot.get("email").toString());
                                user.setUsername(snapshot.get("username").toString());
                                user.setPicUrl(snapshot.get("picUrl").toString());
                                mRequests.add(user);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ImageViewHolder> {
        private Context mContext;
        private List<User> mRequests;

        public RequestAdapter(Context context, List<User> users) {
            mContext = context;
            mRequests = users;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.request_item, parent, false);
            return new ImageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, final int position) {

            User user = mRequests.get(position);
            holder.Name.setText(user.getName());
            holder.Username.setText(user.getUsername());
            holder.Accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = mRequests.get(position).getId();
                    acceptRequest(id);
                }
            });

            holder.Reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = mRequests.get(position).getId();
                    rejectRequest(id);
                }
            });
            Picasso.with(FriendRequestsActivity.this)
                    .load(user.getPicUrl())
                    .placeholder(R.drawable.user_vector)
                    .into(holder.Icon);
        }

        @Override
        public int getItemCount() {
            return mRequests.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView Icon;
            TextView Name, Username;
            ImageView Accept;
            ImageView Reject;

            public ImageViewHolder(View itemView) {
                super(itemView);
                Icon = itemView.findViewById(R.id.icon);
                Name = itemView.findViewById(R.id.name);
                Username = itemView.findViewById(R.id.username);
                Accept = itemView.findViewById(R.id.Accept);
                Reject = itemView.findViewById(R.id.Reject);
            }
        }

    }

    private void acceptRequest(final String senderId) {
        final String currentUserID = GlobalClass.LoggedInUser.getId();
        FirebaseFirestore.getInstance().collection("users/" + currentUserID + "/requests")
                .document(senderId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, String> sender = new HashMap<>(), currentUser = new HashMap<>();
                        sender.put("id", senderId);
                        currentUser.put("id", currentUserID);
                        FirebaseFirestore.getInstance().collection("users/" + currentUserID + "/friends").document(senderId).set(sender);
                        FirebaseFirestore.getInstance().collection("users/" + senderId + "/friends").document(currentUserID).set(currentUser);
                        Toast.makeText(FriendRequestsActivity.this, "request accepted", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rejectRequest(String senderId) {
        final String currentUserID = GlobalClass.LoggedInUser.getId();
        FirebaseFirestore.getInstance().collection("users/" + currentUserID + "/requests")
                .document(senderId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(FriendRequestsActivity.this, "request rejected", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupNavigation() {
        LeftNavText = findViewById(R.id.left_nav_text);
        LeftNavText.setPaintFlags(LeftNavText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        LeftNavBtn = findViewById(R.id.LeftNavBtn);

        LeftNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RootChatsActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), RootChatsActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}