package com.android.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    RecyclerView mRecyclerView;
    ChatsAdapter mAdapter;
    List<User> mChats = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chats, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ChatsAdapter(getActivity(), mChats);
        mRecyclerView.setAdapter(mAdapter);
        updateChats();
        return view;
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
            String from = user.getName() + " (@ " + user.getUsername() + ")";
            holder.From.setText(from);
            holder.MessageSubject.setText("Start new chat");
            holder.Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GlobalClass.mSelectedUser = list.get(position);
                    startActivity(new Intent(getActivity(), MessageActivity.class));
                }
            });

            Picasso.with(getActivity())
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
}


