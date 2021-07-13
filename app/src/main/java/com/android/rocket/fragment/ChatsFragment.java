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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.rocket.R;
import com.android.rocket.activity.MessageActivity;
import com.android.rocket.model.Friend;
import com.android.rocket.util.Constants;
import com.android.rocket.util.FileUtil;
import com.android.rocket.util.Session;
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
    List<Friend> mFriends = new ArrayList<>();
    public static final String TAG = "ChatsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chats, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ChatsAdapter(getActivity(), mFriends);
        mRecyclerView.setAdapter(mAdapter);
        updateChats();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    try {
                        JSONArray friends = new JSONArray(responseData);
                        int length = friends.length();
                        mFriends.clear();
                        for (int i = 0; i < length; i++) {
                            JSONObject friendJSONObject = friends.getJSONObject(i);
                            JSONObject recentMessageJSONObject;
                            Friend.RecentMessage recentMessage = null;
                            try{
                                recentMessageJSONObject = friendJSONObject.getJSONObject("recentMessage");
                                recentMessage = new Friend.RecentMessage(
                                        recentMessageJSONObject.getInt("senderId"),
                                        recentMessageJSONObject.getInt("receiverId"),
                                        recentMessageJSONObject.getString("text"),
                                        recentMessageJSONObject.getInt("unseenCount"),
                                        recentMessageJSONObject.getString("dateSent"),
                                        recentMessageJSONObject.getBoolean("seen")
                                );
                            } catch (JSONException e){

                            }
                            Friend friend = new Friend(
                                    friendJSONObject.getInt("userId"),
                                    friendJSONObject.getString("username"),
                                    friendJSONObject.getString("picture"),
                                    friendJSONObject.getLong("pictureVersion"),
                                    recentMessage
                            );
                            mFriends.add(friend);
                        }
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
        private final Context mContext;
        private final List<Friend> friends;

        public ChatsAdapter(Context context, List<Friend> friends) {
            mContext = context;
            this.friends = friends;
        }

        @Override
        public ChatsAdapter.ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chats_item, parent, false);
            return new ChatsAdapter.ChatsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ChatsAdapter.ChatsViewHolder holder, final int position) {

            Friend friend = friends.get(position);
            String from = friend.getUsername();
            holder.From.setText(from);
            holder.SeenCheck.setVisibility(View.GONE);
            boolean rmExists = friend.getRecentMessage() != null;
            holder.RecentMessageText.setText(rmExists ? friend.getRecentMessage().getText() : "start new chat");
            holder.Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Session.SelectedFriend = friends.get(position);
                    startActivity(new Intent(getActivity(), MessageActivity.class));
                    getActivity().overridePendingTransition(0, 0);
                    getActivity().finish();
                }
            });
            if (rmExists && friend.getRecentMessage().getSenderId() == Session.LoggedInUser.getUserId()) {
                holder.SeenCheck.setVisibility(View.VISIBLE);
                if (friend.getRecentMessage().isSeen())
                    holder.SeenCheck.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.seen_vector));
                else
                    holder.SeenCheck.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.delivered_vector));
            } else {
                if (rmExists && !friend.getRecentMessage().isSeen()) {
                    holder.RecentMessageText.setTextColor(ContextCompat.getColor(mContext, R.color.text_color));
                }
            }
            File picture = FileUtil.getImageFileUserData(mContext, friend.getUserId(), friend.getUsername(), friend.getPicture(), friend.getPictureVersion());
            Picasso.with(getActivity())
                    .load(picture)
                    .placeholder(R.drawable.user_vector)
                    .into(holder.Icon);
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        public class ChatsViewHolder extends RecyclerView.ViewHolder {

            ImageView Icon, SeenCheck;
            TextView From, RecentMessageText, Time, UnreadCount;
            RelativeLayout Item;

            public ChatsViewHolder(View itemView) {
                super(itemView);
                Item = itemView.findViewById(R.id.item);
                Icon = itemView.findViewById(R.id.icon);
                SeenCheck = itemView.findViewById(R.id.seen_check);
                From = itemView.findViewById(R.id.name);
                RecentMessageText = itemView.findViewById(R.id.recent_message_text);
                Time = itemView.findViewById(R.id.time);
                UnreadCount = itemView.findViewById(R.id.unread_count);
            }
        }
    }
}


