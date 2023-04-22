package com.eugeproger.coconet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.Utility;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.eugeproger.coconet.tabs.adapter.TabsAccessorAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AppActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private DatabaseReference rootReference;
    private FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CoCoNet");

        viewPager = findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);

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
            sendUserToLoginActivity();
        } else {
            VerifyUserExistence();
        }
    }

    private void VerifyUserExistence() {
        String currentUserID = auth.getCurrentUser().getUid();
        rootReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("name").exists())) {
                    Utility.showShortToast(AppActivity.this, "Welcome");
                } else {
                    sendUserToSettingsActivity();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(AppActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(AppActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
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

        if (item.getItemId() == R.id.main_logout_option) {
            auth.signOut();
            finish();
            sendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option) {
            sendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_find_user_option) {

        }
        return true;

    }
}