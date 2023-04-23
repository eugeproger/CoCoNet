package com.eugeproger.coconet.tabs.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.DatabaseRealtimeFolderName;
import com.eugeproger.coconet.support.FirebaseConfiguration;
import com.eugeproger.coconet.support.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayTextMessages, title;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private FirebaseAuth auth;
    private DatabaseReference userReference, groupNameReference, groupMessageKeyReference;

    private void initializeElements() {
        toolbar = findViewById(R.id.group_chat_bar_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        title = toolbar.findViewById(R.id.title);
        title.setText(currentGroupName);

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessages = findViewById(R.id.display_group_chat_message);
        scrollView = findViewById(R.id.scroll_group_chat);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get(Constant.GROUP_NAME).toString();

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        userReference = FirebaseConfiguration.setRealtimeDatabaseConfiguration().child(DatabaseRealtimeFolderName.USERS);
        groupNameReference = FirebaseConfiguration.setRealtimeDatabaseConfiguration().child(DatabaseRealtimeFolderName.GROUPS).child(currentGroupName);

        initializeElements();

        getUserInfo();

        sendMessageButton.setOnClickListener(view -> {
                saveMessageInfoToDatabase();
                userMessageInput.setText("");
        });
    }

    private void getUserInfo() {
        userReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child(Constant.NAME).getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameReference.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Utility.showShortToast(this, "Write message");
        } else {
            Calendar calendarForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd, mm, yyyyfff");
            currentDate = currentDateFormat.format(calendarForDate.getTime());

            Calendar calendarForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
            currentTime = currentTimeFormat.format(calendarForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameReference.updateChildren(groupMessageKey);

            groupMessageKeyReference = groupNameReference.child(messageKey);

            HashMap<String, Object> messagesInfo = new HashMap<>();
            messagesInfo.put(Constant.NAME, currentUserName);
            messagesInfo.put(Constant.MESSAGE, message);
            messagesInfo.put(Constant.DATE, currentDate);
            messagesInfo.put(Constant.TIME, currentTime);

            groupMessageKeyReference.updateChildren(messagesInfo);
        }
    }

}