package com.eugeproger.coconet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eugeproger.coconet.option.MyProfileActivity;
import com.eugeproger.coconet.option.SearchUserActivity;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.FirebaseConfiguration;
import com.eugeproger.coconet.support.FirebaseFolderName;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileBio;
    private Button sendMessageRequestButton;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userProfileImage = findViewById(R.id.avatar_activity_profile);
        userProfileName = findViewById(R.id.user_name_activity_profile);
        userProfileBio = findViewById(R.id.user_bio_activity_profile);
        sendMessageRequestButton = findViewById(R.id.send_message_button_activity_profile);


        receiverUserID = getIntent().getExtras().get(Constant.VISIT_USER_ID).toString();
        userRef = FirebaseConfiguration.setRealtimeDatabaseConfiguration().child(FirebaseFolderName.USERS);

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

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileBio.setText(userBio);
                } else {

                    String userName = snapshot.child(Constant.NAME).getValue().toString();
                    String userBio = snapshot.child(Constant.BIO).getValue().toString();

                    userProfileName.setText(userName);
                    userProfileBio.setText(userBio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}