package com.eugeproger.coconet.tabs.chat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;

    private void initializeElements() {

        chatToolBar = findViewById(R.id.tollBar_chatAct);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionView = layoutInflater.inflate(R.layout.layout_chat_bar, null);
        actionBar.setCustomView(actionView);

        userImage = findViewById(R.id.imaga_layChatBar);
        userLastSeen = findViewById(R.id.last_seen_user_layChatBar);
        userName = findViewById(R.id.name_profile_layChatBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get(Constant.VISIT_USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(Constant.VISIT_USER_NAME).toString();
        messageReceiverImage = getIntent().getExtras().get(Constant.VISIT_USER_IMAGE).toString();

        initializeElements();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

    }


}