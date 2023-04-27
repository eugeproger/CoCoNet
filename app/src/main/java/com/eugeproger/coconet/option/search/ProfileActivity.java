package com.eugeproger.coconet.option.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID, currentState, senderUserID;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileBio;
    private Button sendMessageRequestButton, declineMessageRequestButton;
    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userProfileImage = findViewById(R.id.avatar_activity_profile);
        userProfileName = findViewById(R.id.user_name_activity_profile);
        userProfileBio = findViewById(R.id.user_bio_activity_profile);
        sendMessageRequestButton = findViewById(R.id.send_message_button_activity_profile);
        declineMessageRequestButton = findViewById(R.id.decline_message_button_activity_profile);
        currentState = Constant.NEW_REQUEST;

        receiverUserID = getIntent().getExtras().get(Constant.VISIT_USER_ID).toString();
        userRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.USERS);
        chatRequestRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.CHAT_REQUESTS);
        contactsRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.CONTACTS);
        notificationRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.NOTIFICATIONS);
        auth = FirebaseAuth.getInstance();
        senderUserID = auth.getCurrentUser().getUid();

        retrieveUserInformation();
    }

    private void retrieveUserInformation() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild(Constant.IMAGE))) {

                    String userImage = snapshot.child(Constant.IMAGE).getValue().toString();
                    String userName = snapshot.child(Constant.NAME).getValue().toString();
                    String userBio = snapshot.child(Constant.BIO).getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.avatar_profile).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileBio.setText(userBio);

                    manageChatRequests();

                } else {

                    String userName = snapshot.child(Constant.NAME).getValue().toString();
                    String userBio = snapshot.child(Constant.BIO).getValue().toString();

                    userProfileName.setText(userName);
                    userProfileBio.setText(userBio);

                    manageChatRequests();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void manageChatRequests() {
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)) {
                    String requestType = snapshot.child(receiverUserID).child(Constant.REQUEST_TYPE).getValue().toString();

                    if (requestType.equals(Constant.SENT)) {
                        currentState = Constant.SENT_REQUEST;
                        sendMessageRequestButton.setText("Cancel chat request");
                    } else if (requestType.equals(Constant.RECEIVED)) {
                        currentState = Constant.RECEIVED_REQUEST;
                        sendMessageRequestButton.setText("Accept chat request");
                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);
                        declineMessageRequestButton.setOnClickListener(view -> {
                            cancelChatRequest();
                        });
                    }
                } else {
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserID)) {
                                currentState = Constant.CONTACT;
                                sendMessageRequestButton.setText("Remove contact");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (!senderUserID.equals(receiverUserID)) {
            sendMessageRequestButton.setOnClickListener(view -> {
                sendMessageRequestButton.setEnabled(false);
                if (currentState.equals(Constant.NEW_REQUEST)) {
                    sendChatRequest();
                }
                if (currentState.equals(Constant.SENT_REQUEST)) {
                    cancelChatRequest();
                }
                if (currentState.equals(Constant.RECEIVED_REQUEST)) {
                    acceptChatRequest();
                }
                if (currentState.equals(Constant.CONTACT)) {
                    removeContact();
                }
            });
        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeContact() {
        contactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    contactsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task1) {
                            if (task1.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                currentState = Constant.NEW_REQUEST;
                                sendMessageRequestButton.setText("Send message");

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }

                        }
                    });
                }
            }
        });
    }


    private void acceptChatRequest() {
        contactsRef.child(senderUserID).child(receiverUserID).child(NameFolderFirebase.CONTACTS).setValue(Constant.SAVED).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                contactsRef.child(receiverUserID).child(senderUserID).child(NameFolderFirebase.CONTACTS).setValue(Constant.SAVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task1) {
                        if (task1.isSuccessful()) {
                            chatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task2) {
                                    if (task2.isSuccessful()) {
                                        chatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task4) {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = Constant.CONTACT;
                                                sendMessageRequestButton.setText("Remove contact");
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                });

            }
        });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    chatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task1) {
                            if (task1.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                currentState = Constant.NEW_REQUEST;
                                sendMessageRequestButton.setText("Send message");

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }

                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID).child(Constant.REQUEST_TYPE).setValue(Constant.SENT).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRequestRef.child(receiverUserID).child(senderUserID).child(Constant.REQUEST_TYPE).setValue(Constant.RECEIVED).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        HashMap<String, String> chatRequestNotifications = new HashMap<>();
                        chatRequestNotifications.put(Constant.FROM, senderUserID);
                        chatRequestNotifications.put(Constant.TYPE, Constant.REQUEST);

                        notificationRef.child(receiverUserID).push().setValue(chatRequestNotifications).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    sendMessageRequestButton.setEnabled(true);
                                    currentState = Constant.SENT_REQUEST;
                                    sendMessageRequestButton.setText("Cancel chat request");
                                }
                            }
                        });

                    }
                });
            }
        });
    }
}