package com.android.rocket.fragment;

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

import com.android.rocket.util.Session;
import com.android.rocket.R;
import com.android.rocket.activity.GroupDetailsEditor;
import com.android.rocket.activity.GroupMessagingActivity;
import com.android.rocket.modal.GroupInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment {

    RecyclerView mRecyclerView;
    FloatingActionButton floatingActionButton;
    GroupAdapter mAdapter;
    List<GroupInfo> mGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_groups, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GroupDetailsEditor.class));
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new GroupAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        updateGroups();
        return view;
    }

    public void updateGroups() {
//        FirebaseFirestore.getInstance().collection("users/" + Session.LoggedInUser.getId() + "/groups").addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                mAdapter.clear();
//                if (value != null) {
//                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
//                        GroupInfo group = new GroupInfo();
//                        System.out.println("Into Group");
//                        group.setGroupId(documentSnapshot.get("groupId").toString());
//                        group.setGroupName(documentSnapshot.get("groupName").toString());
//                        group.setPicUrl(documentSnapshot.get("picUrl").toString());
//                        mGroups.add(group);
//                        mAdapter.add(group);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//        });
    }

    public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
        private Context mContext;
        private List<GroupInfo> list = new ArrayList<>();

        GroupAdapter(Context context) {
            mContext = context;
        }

        public void add(GroupInfo groupInfo) {
            this.list.add(groupInfo);
        }

        public void clear() {
            this.list.clear();
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.groups_item, parent, false);
            return new GroupViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, final int position) {
            GroupInfo groupInfo = list.get(position);
            String from = groupInfo.getGroupName();
            holder.From.setText(from);
            holder.MessageSubject.setText("Start new chat");
            holder.Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Session.SelectedGroup = list.get(position);
                    startActivity(new Intent(getActivity(), GroupMessagingActivity.class));
                }
            });

            Picasso.with(getActivity())
                    .load(groupInfo.getPicUrl())
                    .placeholder(R.drawable.android_vector)
                    .into(holder.Icon);
        }

        @Override
        public int getItemCount() {
            return this.list.size();
        }

        public class GroupViewHolder extends RecyclerView.ViewHolder {

            ImageView Icon;
            TextView From, MessageSubject, Time, UnreadCount;
            RelativeLayout Item;

            public GroupViewHolder(@NonNull View itemView) {
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
