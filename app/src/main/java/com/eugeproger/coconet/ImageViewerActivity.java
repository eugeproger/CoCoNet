package com.eugeproger.coconet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.eugeproger.coconet.support.Constant;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.main_actImageViewer);
        imageUrl = getIntent().getStringExtra(Constant.URL);

        Picasso.get().load(imageUrl).into(imageView);
    }
}