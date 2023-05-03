package com.eugeproger.coconet.tabs.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.simple.Message;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.eugeproger.coconet.support.Type;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private ImageButton sendMessageButton, attachFileButton;
    private EditText messageInputText;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private final List<Message> messages = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessages;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", mUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

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
        attachFileButton = findViewById(R.id.attach_button_actChatting);

        messageAdapter = new MessageAdapter(messages);
        userMessages = findViewById(R.id.chats_chatAct);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessages.setLayoutManager(linearLayoutManager);
        userMessages.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
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

        start();

        initializeElements();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.avatar_profile).into(userImage);

        sendMessageButton.setOnClickListener(view -> sendMessage());
        displayLastSeen();

        attachFileButton.setOnClickListener(view -> attachFile());
    }


    public void start() {

        rootRef.child(NameFolderFirebase.MESSAGES).child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                messages.add(message);
                messageAdapter.notifyDataSetChanged();

                userMessages.smoothScrollToPosition(userMessages.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); {

        }
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
            messageTextBody.put(Constant.TO, messageReceiverID);
            messageTextBody.put(Constant.MESSAGE_ID, messagePushID);
            messageTextBody.put(Constant.TIME, saveCurrentTime);
            messageTextBody.put(Constant.DATE, saveCurrentDate);

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

    private void displayLastSeen() {
        rootRef.child(NameFolderFirebase.USERS).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(NameFolderFirebase.USER_STATE).hasChild(Constant.STATE)) {
                    String state = snapshot.child(NameFolderFirebase.USER_STATE).child(Constant.STATE).getValue().toString();
                    String date = snapshot.child(NameFolderFirebase.USER_STATE).child(Constant.DATE).getValue().toString();
                    String time = snapshot.child(NameFolderFirebase.USER_STATE).child(Constant.TIME).getValue().toString();

                    if (state.equals(Constant.ONLINE)) {
                        userLastSeen.setText(Constant.ONLINE);
                    } else if (state.equals(Constant.OFFLINE)) {
                        userLastSeen.setText("last seen " + date + " at " + time);
                    }
                } else {
                    userLastSeen.setText(Constant.OFFLINE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void attachFile() {

        CharSequence options[] = new CharSequence[] {
                "Image",
                "PDF File",
                "DOCX File",

        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select a file");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    checker = Constant.IMAGE;

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType(Type.IMAGE);
                    startActivityForResult(intent.createChooser(intent, "Select image"), Constant.REQUEST_CODE_2);
                }
                if (i == 1) {

                    checker = Constant.PDF;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType(Type.APPLICATION_PDF);
                    startActivityForResult(intent.createChooser(intent, "Select PDF file"), Constant.REQUEST_CODE_2);
                }
                if (i == 2) {
                    checker = Constant.DOCX;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType(Type.APPLICATION_DOCX);
                    startActivityForResult(intent.createChooser(intent, "Select DOCX file"), Constant.REQUEST_CODE_2);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_CODE_2 && resultCode == RESULT_OK && data != null) {

            loadingBar.setTitle("Sending file");
            loadingBar.setMessage("Proces...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals(Constant.IMAGE)) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(NameFolderFirebase.DOCUMENT_FILES);
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef
                        .child(NameFolderFirebase.MESSAGES)
                        .child(messageSenderID)
                        .child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Map messageImageBody = new HashMap();
                            messageImageBody.put(Constant.MESSAGE, task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageImageBody.put(Constant.NAME, fileUri.getLastPathSegment());
                            messageImageBody.put(Constant.TYPE, checker);
                            messageImageBody.put(Constant.FROM, messageSenderID);
                            messageImageBody.put(Constant.TO, messageReceiverID);
                            messageImageBody.put(Constant.MESSAGE_ID, messagePushID);
                            messageImageBody.put(Constant.TIME, saveCurrentTime);
                            messageImageBody.put(Constant.DATE, saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Utility.showErrorToast(ChatActivity.this, e.getMessage());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        loadingBar.setMessage((int) progress + "% uploading...");
                    }
                });

            } else if (checker.equals(Constant.IMAGE)) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(NameFolderFirebase.IMAGE_FILES);
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef
                        .child(NameFolderFirebase.MESSAGES)
                        .child(messageSenderID)
                        .child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return  filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            mUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put(Constant.MESSAGE, mUrl);
                            messageImageBody.put(Constant.NAME, fileUri.getLastPathSegment());
                            messageImageBody.put(Constant.TYPE, checker);
                            messageImageBody.put(Constant.FROM, messageSenderID);
                            messageImageBody.put(Constant.TO, messageReceiverID);
                            messageImageBody.put(Constant.MESSAGE_ID, messagePushID);
                            messageImageBody.put(Constant.TIME, saveCurrentTime);
                            messageImageBody.put(Constant.DATE, saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Utility.showShortToast(ChatActivity.this, "Message sent successfully");

                                    } else {
                                        loadingBar.dismiss();
                                        Utility.showLengthToast(ChatActivity.this, "Error");
                                    }
                                    messageInputText.setText("");
                                }
                            });

                        }
                    }
                });

            } else {
                loadingBar.dismiss();
                Utility.showLengthToast(this, "Nothing selected");
            }
        }
    }
}