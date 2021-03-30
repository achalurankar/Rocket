package com.android.rocket.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import com.android.rocket.util.GlobalClass;
import com.android.rocket.R;
import com.android.rocket.util.SectionsPagerAdapter;
import com.android.rocket.modal.User;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RootChatsActivity extends AppCompatActivity {

    RootChatsActivity mInstance = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_chats);
        setupNavigation();
        TextView CurrentUsername;
        TextView Logout;
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
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    //method to set up navigation
    private void setupNavigation() {
        TextView LeftNavText;
        TextView RightNavText;
        LinearLayout LeftNavBtn;
        LinearLayout RightNavBtn;
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