package com.android.rocket.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.rocket.R;
import com.squareup.picasso.Picasso;

public class SelectedImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image);
        if(MessageActivity.mSelectedImageUrl == null){
            Toast.makeText(this, "Image cannot be displayed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView imageView = findViewById(R.id.selected_image);

        Picasso.with(this)
                .load(MessageActivity.mSelectedImageUrl)
                .placeholder(R.drawable.user_vector)
                .into(imageView);
    }
}