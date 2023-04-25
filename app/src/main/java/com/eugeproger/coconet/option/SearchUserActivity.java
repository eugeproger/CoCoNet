package com.eugeproger.coconet.option;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.eugeproger.coconet.R;

public class SearchUserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView searchUsersRecyclerView;
    private TextView titleToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchUsersRecyclerView = findViewById(R.id.search_user_recyclerView);
        searchUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.search_user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        titleToolBar = toolbar.findViewById(R.id.title);
        titleToolBar.setText("Search user");
    }
}