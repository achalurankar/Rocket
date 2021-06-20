package com.android.rocket.activity;

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
 * Friend requests activity right to home screen
 */
public class FriendRequestsActivity extends AppCompatActivity {

    RequestAdapter mAdapter;
    RecyclerView mRecyclerView;
    List<User> mRequests = new ArrayList<>();
    TextView LeftNavText;
    LinearLayout LeftNavBtn;
    OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        mClient = new OkHttpClient.Builder().build();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RequestAdapter(this, mRequests);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        setupNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRequests();
    }

    private void getRequests() {
        Request request = new Request.Builder()
                .url(Constants.host + "/request/" + Session.LoggedInUser.getUserId())
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    try {
                        JSONArray array = new JSONArray(responseData);
                        int length = array.length();
                        mRequests.clear();
                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject = (JSONObject) array.get(i);
                            User user = new User(
                                    jsonObject.getInt("userId"),
                                    jsonObject.getString("username"),
                                    jsonObject.getString("emailId"),
                                    jsonObject.getString("picture"));
                            mRequests.add(user);
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

            final User user = mRequests.get(position);
            holder.Name.setText(user.getUsername());
            holder.Username.setText(user.getEmail());
            holder.Accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    respondToRequest(user, true);
                }
            });

            holder.Reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    respondToRequest(user, false);
                }
            });
            Picasso.with(FriendRequestsActivity.this)
                    .load(user.getPicture())
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

    private void respondToRequest(User friend, boolean accepted) {
        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put("userId", Session.LoggedInUser.getUserId());
            wrapper.put("friendId", friend.getUserId());
            wrapper.put("accepted", accepted);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(Client.JSON, String.valueOf(wrapper));
        Request request = new Request.Builder()
                .method("POST", requestBody)
                .url(Constants.host + "/request/respond")
                .addHeader("Content-Type", "application/json")
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = "";
                            if (response.code() == 200) {
                                message = "User added";
                            }else {
                                message = "Cannot add this user!";
                            }
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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