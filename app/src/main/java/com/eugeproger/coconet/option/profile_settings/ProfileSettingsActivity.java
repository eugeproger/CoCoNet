package com.eugeproger.coconet.option.profile_settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.eugeproger.coconet.AppMainActivity;
import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingsActivity extends AppCompatActivity {
    private Button updateAccountSettings;
    private EditText userName, userBio;
    private TextView titleActivity;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImagesRef;
    private ProgressDialog loadingBar;
    private StorageReference filePath;
    private Toolbar toolbar;

    private void initializeElements() {
        updateAccountSettings = (Button) findViewById(R.id.my_update_settings_button);
        userName = (EditText) findViewById(R.id.my_user_name_set);
        userBio = (EditText) findViewById(R.id.my_user_bio_set);
        userProfileImage = findViewById(R.id.my_profileActivity_imageView);


        toolbar =findViewById(R.id.tool_bar_settingsAct);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        titleActivity = toolbar.findViewById(R.id.title);
        titleActivity.setText("Profile settings");

        loadingBar = new ProgressDialog(this);

    }

    private void databaseConfigurations() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = ConfigurationFirebase.setRealtimeDatabaseRef();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child(NameFolderFirebase.PROFILE_IMAGES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        initializeElements();
        databaseConfigurations();

        updateAccountSettings.setOnClickListener(view -> updateProfile());
        retrieveUserInfo();

        userProfileImage.setOnClickListener(view -> sendUserToGallery());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Constant.GALLERY_PICK  &&  resultCode==RESULT_OK  &&  data!=null) {
            Uri ImageUri = data.getData();
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set profile image");
                loadingBar.setMessage("Profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                filePath = userProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Utility.showShortToast(ProfileSettingsActivity.this, "Profile image is uploaded successfully!");

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    rootRef.child(NameFolderFirebase.USERS).child(currentUserID).child(Constant.IMAGE).setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Utility.showShortToast(ProfileSettingsActivity.this, "Image is saved in database successfully!");
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().toString();
                                                Utility.showErrorToast(ProfileSettingsActivity.this, message);
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                                }

                            });
                        } else {
                            String message = task.getException().toString();

                            Utility.showErrorToast(ProfileSettingsActivity.this, message);
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void updateProfile() {
        String setUserName = userName.getText().toString();
        String setStatus = userBio.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Utility.showLengthToast(this, "Please write your user name first");
        }
        if (TextUtils.isEmpty(setStatus)) {
            Utility.showLengthToast(this, "Please write your bio");
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(Constant.UID, currentUserID);
            profileMap.put(Constant.NAME, setUserName);
            profileMap.put(Constant.BIO, setStatus);
            rootRef.child(NameFolderFirebase.USERS).child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Utility.showShortToast(ProfileSettingsActivity.this, "Profile is updated successfully!");
                    } else {
                        String message = task.getException().toString();
                        Utility.showErrorToast(ProfileSettingsActivity.this, message);
                    }
                }
            });
        }
    }

    private void retrieveUserInfo() {
        rootRef.child(NameFolderFirebase.USERS).child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(Constant.NAME) && (dataSnapshot.hasChild(Constant.IMAGE)))) {
                    String retrieveUserName = dataSnapshot.child(Constant.NAME).getValue().toString();
                    String retrievesStatus = dataSnapshot.child(Constant.BIO).getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child(Constant.IMAGE).getValue().toString();

                    userName.setText(retrieveUserName);
                    userBio.setText(retrievesStatus);
                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.avatar_profile).into(userProfileImage);

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(Constant.NAME))) {
                    String retrieveUserName = dataSnapshot.child(Constant.NAME).getValue().toString();
                    String retrievesStatus = dataSnapshot.child(Constant.BIO).getValue().toString();

                    userName.setText(retrieveUserName);
                    userBio.setText(retrievesStatus);
                } else {
                    Utility.showLengthToast(ProfileSettingsActivity.this, "Set and update your profile information.");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileSettingsActivity.this, AppMainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Constant.GALLERY_PICK);
    }
}