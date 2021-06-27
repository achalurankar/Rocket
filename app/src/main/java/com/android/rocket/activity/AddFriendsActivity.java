package com.android.rocket.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.rocket.R;
import com.android.rocket.modal.User;
import com.android.rocket.util.Client;
import com.android.rocket.util.Constants;
import com.android.rocket.util.Session;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Add friends activity, left to home screen
 */

public class AddFriendsActivity extends AppCompatActivity {

    UserAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<User> mUsers = new ArrayList<>();
    EditText SearchBarET;
    TextView RightNavText;
    LinearLayout RightNavBtn;
    public static final String TAG = "AddFriendsActivity";
    OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new OkHttpClient.Builder().build();
        setContentView(R.layout.activity_add_friends);
        SearchBarET = findViewById(R.id.SearchBarET);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UserAdapter(AddFriendsActivity.this, mUsers);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        getUsers();
        setupNavigation();
    }

    private void getUsers() {
        Request request = new Request.Builder()
                .url(Constants.host + "/users/" + Session.LoggedInUser.getUserId())
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "onFailure: getUsers() exception message :" + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    try {
                        JSONArray array = new JSONArray(responseData);
                        int length = array.length();
                        mUsers.clear();
                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject = (JSONObject) array.get(i);
                            User user = new User(
                                    jsonObject.getInt("userId"),
                                    jsonObject.getString("username"),
                                    jsonObject.getString("emailId"),
                                    jsonObject.getString("picture"),
                                    jsonObject.getLong("picture_version")
                            );
                            mUsers.add(user);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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

            final User user = list.get(position);
            holder.Name.setText(user.getUsername());
            holder.Username.setText(user.getEmailId());
            holder.AddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addUser(user);
                }
            });
            Picasso.with(mContext)
                    .load(user.getPicture())
                    .placeholder(R.drawable.user_vector)
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

    private void addUser(User friend) {
        //request object
        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put("userId", Session.LoggedInUser.getUserId());
            wrapper.put("friendId", friend.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(Client.JSON, String.valueOf(wrapper));
        Request request = new Request.Builder()
                .method("POST", requestBody)
                .url(Constants.host + "/request")
                .addHeader("Content-Type", "application/json")
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddFriendsActivity.this, "User added", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddFriendsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
                startActivity(new Intent(getApplicationContext(), RootChatsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), RootChatsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}