package com.android.chatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.chatapp.R;
import com.android.chatapp.controller.AppController;
import com.android.chatapp.service.LastSeenUpdater;
import com.android.chatapp.util.GlobalClass;
import com.android.chatapp.util.TokenRefresher;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Splash screen activity
 */
public class SplashScreen extends AppCompatActivity {

    public static final int ANIM_DURATION = 600;
    ImageView Rocket;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Rocket = findViewById(R.id.rocket);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        animation = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.rocket_launch);
        setController();
        checkUser();
    }

    private void checkUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            GlobalClass.LoggedInUser.setEmail(Email);
            FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("email", Email)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                GlobalClass.LoggedInUser.setName(documentSnapshot.get("name").toString());
                                GlobalClass.LoggedInUser.setUsername(documentSnapshot.get("username").toString());
                                GlobalClass.LoggedInUser.setId(documentSnapshot.get("id").toString());
                                GlobalClass.LoggedInUser.setEmail(documentSnapshot.get("email").toString());
                                GlobalClass.LoggedInUser.setPicUrl(documentSnapshot.get("picUrl").toString());
                                TokenRefresher.updateToken(GlobalClass.LoggedInUser.getId());
                            }
                            start();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(SplashScreen.this, RootChatsActivity.class));
                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                    finish();
                                }
                            }, ANIM_DURATION + 100);
                        }
                    });
        } else {
            start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();
                }
            }, ANIM_DURATION + 100);
        }
    }

    private void setController() {
        AppController.getInstance().setOnVisibilityChangeListener(new AppController.ValueChangeListener() {
            @Override
            public void onChanged(Boolean isBackground) {
                //to update last seen
                Intent foregroundIntent = new Intent(getApplicationContext(), LastSeenUpdater.class);
                foregroundIntent.putExtra("isAppBackground", isBackground);
                startService(foregroundIntent);
                //to start notification channel
//                Intent foregroundServiceNotificationIntent = new Intent(getApplicationContext(), NotificationForeground.class);
//                if(isBackground){
//                    ContextCompat.startForegroundService(getApplicationContext(), foregroundServiceNotificationIntent);
//                }else {
//                    stopService(foregroundServiceNotificationIntent);
//                }
            }
        });
    }

    private void start() {
        Rocket.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Rocket.setAlpha(0f);
            }
        }, ANIM_DURATION);
    }
}