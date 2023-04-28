package com.eugeproger.coconet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.eugeproger.coconet.login.LoginActivity;
import com.eugeproger.coconet.option.profile_settings.ProfileSettingsActivity;
import com.eugeproger.coconet.option.search.SearchUserActivity;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.eugeproger.coconet.tabs.adapter.TabsAccessorAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AppMainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private DatabaseReference rootReference;
    private FirebaseDatabase firebaseDatabase;
    private String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main);

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);
        viewPager.setCurrentItem(0, false);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);


        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance(Constant.REALTIME_DATABASE_LINK);
        rootReference = firebaseDatabase.getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            holdUserToLoginActivity();
        } else {
            updateUserStatus(Constant.ONLINE);
            verifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null) {
            updateUserStatus(Constant.OFFLINE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser != null) {
            updateUserStatus(Constant.OFFLINE);
        }
    }

    private void verifyUserExistence() {
        currentUserID = auth.getCurrentUser().getUid();
        rootReference.child(NameFolderFirebase.USERS).child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(Constant.NAME).exists()) {

                } else {
                    holdUserOnProfileActivity();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendUserToMyProfileActivity() {
        Intent profileIntent = new Intent(AppMainActivity.this, ProfileSettingsActivity.class);
        startActivity(profileIntent);
    }

    private void holdUserOnProfileActivity() {
        finish();
        Intent profileIntent = new Intent(AppMainActivity.this, ProfileSettingsActivity.class);
        startActivity(profileIntent);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

    }

    private void holdUserToLoginActivity() {
        Intent loginIntent = new Intent(AppMainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSearchUserActivity() {
        Intent searchUserIntent= new Intent(AppMainActivity.this, SearchUserActivity.class);
        startActivity(searchUserIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_search_user_option) {
            sendUserToSearchUserActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option) {
            RequestNewGroup();
        }
        if (item.getItemId() == R.id.main_profile_option) {
            sendUserToMyProfileActivity();
        }
        if (item.getItemId() == R.id.main_logout_option) {
            auth.signOut();
            finish();
            holdUserToLoginActivity();
        }
        return true;

    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppMainActivity.this, R.style.AlertDialog);

        final EditText groupNameField = new EditText(AppMainActivity.this);
        groupNameField.setHint("Group name");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Utility.showLengthToast(AppMainActivity.this, "Group is not created. Please, write a name of group.");
                } else {
                    createGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void createGroup(String groupName) {
        rootReference.child(NameFolderFirebase.GROUPS).child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showLengthToast(AppMainActivity.this, groupName + "group is created successfully.");
                }
            }
        });
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd, mm, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStates = new HashMap<>();
        onlineStates.put(Constant.TIME, saveCurrentTime);
        onlineStates.put(Constant.DATE, saveCurrentDate);
        onlineStates.put(Constant.STATE, state);

        currentUserID = auth.getCurrentUser().getUid();
        rootReference.child(NameFolderFirebase.USERS).child(currentUserID).child(NameFolderFirebase.USER_STATE).updateChildren(onlineStates);
    }
}