package com.android.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
 *  Add friends activity, left to home screen
 * */

public class AddFriendsActivity extends AppCompatActivity {

    UserAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<User> mUsers = new ArrayList<>();
    EditText SearchBarET;
    List<User> mSearchResultList = new ArrayList<>();
    TextView RightNavText;
    LinearLayout RightNavBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        SearchBarET = findViewById(R.id.SearchBarET);
        setupNavigation();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getUsers();
        SearchBarET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mUsers.size() != 0 && charSequence.length() != 0) {
                    mSearchResultList.clear();
                    for (User user : mUsers) {
                        if (user.getUsername().contains(charSequence) || user.getName().contains(charSequence)) {
                            mSearchResultList.add(user);
                        }
                    }
                    mAdapter = new UserAdapter(AddFriendsActivity.this, mSearchResultList);
                    mAdapter.setHasStableIds(true);
                    mRecyclerView.setAdapter(mAdapter);
                } else if (charSequence.length() == 0) {
                    mRecyclerView.setAdapter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void getUsers() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        mUsers.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.get("email").toString().equals(GlobalClass.LoggedInUser.getEmail())) {
                                User user = new User();
                                user.setId(documentSnapshot.get("id").toString());
                                user.setEmail(documentSnapshot.get("email").toString());
                                user.setName(documentSnapshot.get("name").toString());
                                user.setUsername(documentSnapshot.get("username").toString());
                                user.setPicUrl(documentSnapshot.get("picUrl").toString());
                                mUsers.add(user);
                            }
                        }
                    }
                });
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
                    addFriend(id, name);
                }
            });
            Picasso.with(AddFriendsActivity.this)
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

    private void addFriend(String id, final String name) {
        String currentUserId = GlobalClass.LoggedInUser.getId();
        Map<String, String> map = new HashMap<>();
        map.put("id", currentUserId);
        map.put("accepted", "0"); // not in use currently
        FirebaseFirestore.getInstance().collection("users/" + id + "/requests")
                .document(currentUserId)
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddFriendsActivity.this, name + " Added", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupNavigation() {
        RightNavText = findViewById(R.id.right_nav_text);
        RightNavText.setPaintFlags(RightNavText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        RightNavBtn = findViewById(R.id.RightNavBtn);

        RightNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}