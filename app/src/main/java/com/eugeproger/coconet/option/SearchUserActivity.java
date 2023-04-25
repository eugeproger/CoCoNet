package com.eugeproger.coconet.option;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eugeproger.coconet.R;
import com.eugeproger.coconet.simple.Contact;
import com.eugeproger.coconet.support.FirebaseConfiguration;
import com.eugeproger.coconet.support.FirebaseFolderName;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView searchUsersRecyclerView;
    private TextView titleToolBar;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        userRef = FirebaseConfiguration.setRealtimeDatabaseConfiguration().child(FirebaseFolderName.USERS);

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

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(userRef, Contact.class).build();

        FirebaseRecyclerAdapter<Contact, SearchUserViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, SearchUserViewHolder>(options) {
            @NonNull
            @Override
            public SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_box_layout, parent,false);
                SearchUserViewHolder viewHolder = new SearchUserViewHolder(view);
                return viewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull SearchUserViewHolder holder, int position, @NonNull Contact model) {
                holder.userName.setText(model.getName());
                holder.userBio.setText(model.getBio());
                if (holder.profileImage != null && model.getImage() != null) {
                    Glide.with(SearchUserActivity.this).load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                }
            }
        };
        searchUsersRecyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    public static class SearchUserViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userBio;
        CircleImageView profileImage;

        public SearchUserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userBio = itemView.findViewById(R.id.user_profile_bio);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}