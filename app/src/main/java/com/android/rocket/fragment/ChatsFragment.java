package com.android.rocket.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.rocket.util.Constants;
import com.android.rocket.util.FileUtil;
import com.android.rocket.util.Session;
import com.android.rocket.R;
import com.android.rocket.activity.MessageActivity;
import com.android.rocket.modal.User;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatsFragment extends Fragment {

    RecyclerView mRecyclerView;
    ChatsAdapter mAdapter;
    List<User> mChats = new ArrayList<>();
    public static final String TAG = "ChatsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chats, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ChatsAdapter(getActivity(), mChats);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateChats();
    }

    //method to update chats
    private void updateChats() {

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(Constants.host + "/friends/" + Session.LoggedInUser.getUserId())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "onFailure: updateChats() exception message :" + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseData = response.body().string();
                    try{
                        JSONArray array = new JSONArray(responseData);
                        int length = array.length();
                        mChats.clear();
                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject = (JSONObject) array.get(i);
                            User user = new User(
                                    jsonObject.getInt("userId"),
                                    jsonObject.getString("username"),
                                    jsonObject.getString("emailId"),
                                    jsonObject.getString("picture"),
                                    jsonObject.getLong("pictureVersion")
                            );
                            mChats.add(user);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
        private Context mContext;
        private List<User> list;

        public ChatsAdapter(Context context, List<User> users) {
            mContext = context;
            list = users;
        }

        @Override
        public ChatsAdapter.ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chats_item, parent, false);
            return new ChatsAdapter.ChatsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ChatsAdapter.ChatsViewHolder holder, final int position) {

            User user = list.get(position);
            String from = user.getUsername();
            holder.From.setText(from);
            holder.MessageSubject.setText("Start new chat");
            holder.Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Session.SelectedUser = list.get(position);
                    startActivity(new Intent(getActivity(), MessageActivity.class));
                }
            });

            File picture = FileUtil.getImageFileUserData(mContext, user);
            Picasso.with(getActivity())
                    .load(picture)
                    .placeholder(R.drawable.user_vector)
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
}


