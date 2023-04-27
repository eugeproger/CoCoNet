package com.eugeproger.coconet.tabs.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private ImageButton sendMessageButton;
    private EditText messageInputText;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

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

        sendMessageButton = findViewById(R.id.send_message_button_chatAct);
        messageInputText = findViewById(R.id.input_field_chatAct);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        messageSenderID = auth.getCurrentUser().getUid();
        rootRef = ConfigurationFirebase.setRealtimeDatabaseRef();


        messageReceiverID = getIntent().getExtras().get(Constant.VISIT_USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(Constant.VISIT_USER_NAME).toString();
        messageReceiverImage = getIntent().getExtras().get(Constant.VISIT_USER_IMAGE).toString();

        initializeElements();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.avatar_profile).into(userImage);

        sendMessageButton.setOnClickListener(view -> sendMessage());

    }

    private void sendMessage() {
        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Utility.showLengthToast(this, "Enter message");

        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef
                    .child(NameFolderFirebase.MESSAGES)
                    .child(messageSenderID)
                    .child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put(Constant.MESSAGE, messageText);
            messageTextBody.put(Constant.TYPE, Constant.TEXT);
            messageTextBody.put(Constant.FROM, messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {
                        Utility.showShortToast(ChatActivity.this, "Message sent successfully");

                    } else {
                        Utility.showLengthToast(ChatActivity.this, "Error");
                    }
                    messageInputText.setText("");
                }
            });


        }
    }


}