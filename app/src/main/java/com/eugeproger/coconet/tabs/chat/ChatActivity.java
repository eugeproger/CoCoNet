package com.eugeproger.coconet.tabs.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.Utility;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get(Constant.VISIT_USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(Constant.VISIT_USER_NAME).toString();

        Utility.showLengthToast(ChatActivity.this, messageReceiverID);
        Utility.showLengthToast(ChatActivity.this, messageReceiverName);
    }
}