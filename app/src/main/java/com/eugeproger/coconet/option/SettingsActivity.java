package com.eugeproger.coconet.option;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eugeproger.coconet.AppMainActivity;
import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.FirebaseFolderName;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userBio;
    private ImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth auth;
    private DatabaseReference rootReference;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;

    private void initializeFragments() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.user_name_set);
        userBio = findViewById(R.id.user_bio_set);
        userProfileImage = findViewById(R.id.user_profile_imageView);
        loadingBar = new ProgressDialog(this);
    }

    private void configureFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance(Constant.REALTIME_DATABASE_LINK);
        rootReference = firebaseDatabase.getReference();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child(FirebaseFolderName.PROFILE_IMAGES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeFragments();
        configureFirebase();

        updateAccountSettings.setOnClickListener(view -> updateSettings());
        retrieveUserInformation();

        userProfileImage.setOnClickListener(view -> {
            sendUserToGallery();
            setProfileImage();
        });
    }



    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserBio = userBio.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Utility.showLongToast(this, "Please, write your user name");
        }
        if (TextUtils.isEmpty(setUserBio)) {
            Utility.showLongToast(this, "Please, write your bio information");
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("bio", setUserBio);
            rootReference.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendUserToAppActivity();
                        Utility.showShortToast(SettingsActivity.this, "Profile updated successfully");
                    } else {
                        String message = task.getException().toString();
                        Utility.showLongToast(SettingsActivity.this, "Error: " + message);
                    }
                }
            });
        }

    }

    private void retrieveUserInformation() {
        rootReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image"))) {

                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserBio = snapshot.child("bio").getValue().toString();
                    String retrieveUserProfileImage = snapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userBio.setText(retrieveUserBio );

                } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {

                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserBio = snapshot.child("bio").getValue().toString();

                    userName.setText(retrieveUserName);
                    userBio.setText(retrieveUserBio );

                } else {
                    //userName.setVisibility(View.VISIBLE);
                    Utility.showLongToast(SettingsActivity.this, "Please, update your profile information.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setProfileImage() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            Glide.with(this).load(imageUri).centerCrop().circleCrop().into(userProfileImage);

            StorageReference filePath = userProfileImageReference.child(currentUserID + ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    Utility.showShortToast(SettingsActivity.this, "Profile image uploaded successfully");

                } else {

                    String message = task.getException().toString();
                    Utility.showLongToast(SettingsActivity.this, "Error: " + message);
                }


            });
        }
    }
//                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            final String downloaedUrl = uri.toString();
//                            rootReference.child("Users").child(currentUserID).child("image").setValue(downloaedUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(SettingsActivity.this, "Image saved in Database successfully...", Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            } else {
//                                                String message = task.getException().toString();
//                                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//                                    });
//                        }
//                    });
//                } else {
//                    String message = task.getException().toString();
//                    Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
//                    loadingBar.dismiss();
//                }
//            });
//        }
//    }



    private void sendUserToAppActivity() {
        Intent appIntent = new Intent(SettingsActivity.this, AppMainActivity.class);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(appIntent);
        finish();
    }

    private void sendUserToGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Constant.GALLERY_PICK);
    }

}
