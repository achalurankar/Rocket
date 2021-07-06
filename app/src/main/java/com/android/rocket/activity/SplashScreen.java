package com.android.rocket.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.rocket.R;
import com.android.rocket.controller.AppController;
import com.android.rocket.service.LastSeenUpdater;
import com.android.rocket.util.Constants;
import com.android.rocket.util.Session;

/**
 * Splash screen activity
 */
public class SplashScreen extends AppCompatActivity {

    public static final int ANIM_DURATION = 600;
    ImageView Rocket;
    Animation animation;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Rocket = findViewById(R.id.rocket);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        animation = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.rocket_launch);
        setController();
        setupDatabase();
        checkUser();
    }

    private void setupDatabase() {
        Session.DbRef = openOrCreateDatabase("rocket", MODE_PRIVATE, null);
        //create table user_profile_pictures
        Session.DbRef.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s(%s int, " +
                "%s int, " +
                "%s VARCHAR);",
                Constants.TABLE_USER_PROFILE_PICTURES,
                Constants.FIELD_USER_ID,
                Constants.FIELD_VERSION,
                Constants.FIELD_FILE_PATH
        ));
        //create table chat_logs_pictures
        Session.DbRef.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s(%s int, " +
                        "%s VARCHAR);",
                Constants.TABLE_CHAT_LOGS_PICTURES,
                Constants.FIELD_MESSAGE_ID,
                Constants.FIELD_FILE_PATH
        ));
        //drop database
    }

    private void checkUser() {
        sharedpreferences = getSharedPreferences(Constants.ROCKET_PREFERENCES, Context.MODE_PRIVATE);
        String responseData = sharedpreferences.getString(Constants.USER_INFO_JSON, null);
        if (responseData != null && Session.saveUserInfo(responseData)) {
            startActivity(new Intent(SplashScreen.this, RootChatsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            openLoginActivity();
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
            }
        });
    }

    public void openLoginActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}, ANIM_DURATION + 100);
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