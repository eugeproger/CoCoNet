package com.eugeproger.coconet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userBio;
    private ImageView userProfileImage;

    private void initializeFragments() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.user_name_set);
        userBio = findViewById(R.id.user_bio_set);
        userProfileImage = findViewById(R.id.user_profile_imageView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFragments();
    }


}