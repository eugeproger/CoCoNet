package com.eugeproger.coconet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.Utility;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get(Constant.VISIT_USER_ID).toString();

        Utility.showLengthToast(this, "User ID: " + receiverUserID);
    }
}