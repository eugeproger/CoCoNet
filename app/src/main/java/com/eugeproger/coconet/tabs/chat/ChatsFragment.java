package com.eugeproger.coconet.tabs.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.simple.Contact;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private View privateChatsView;
    private RecyclerView chats;
    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth auth;
    private String currentUserID = "";

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        chatsRef = ConfigurationFirebase.setRealtimeDatabaseRef()
                .child(NameFolderFirebase.CONTACTS)
                .child(currentUserID);
        usersRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.USERS);

        chats = privateChatsView.findViewById(R.id.view_chatsFrg);
        chats.setLayoutManager(new LinearLayoutManager(getContext()));
        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(chatsRef, Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {
            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_user, parent,false);
                return new ChatsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder,
                                            int position,
                                            @NonNull Contact model) {
                final String userIDs = getRef(position).getKey();
                final String[] image = {Constant.DEFAULT_IMAGE};
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild(Constant.IMAGE)) {
                                image[0] = snapshot.child(Constant.IMAGE).getValue().toString();
                                Picasso.get()
                                        .load(image[0])
                                        .placeholder(R.drawable.avatar_profile)
                                        .into(holder.profileImage);
                            }

                            final String name = snapshot.child(Constant.NAME).getValue().toString();
                            final String bio = snapshot.child(Constant.BIO).getValue().toString();
                            holder.userName.setText(name);

                            if (snapshot.child(NameFolderFirebase.USER_STATE)
                                    .hasChild(Constant.STATE)) {
                                String state = snapshot.child(NameFolderFirebase.USER_STATE)
                                        .child(Constant.STATE)
                                        .getValue()
                                        .toString();
                                String date = snapshot.child(NameFolderFirebase.USER_STATE)
                                        .child(Constant.DATE)
                                        .getValue()
                                        .toString();
                                String time = snapshot.child(NameFolderFirebase.USER_STATE)
                                        .child(Constant.TIME)
                                        .getValue()
                                        .toString();
                                if (state.equals(Constant.ONLINE)) {
                                    holder.userBio.setText(Constant.ONLINE);
                                } else if (state.equals(Constant.OFFLINE)) {
                                    holder.userBio.setText("last seen " + date + " at " + time);
                                }
                            } else {
                                holder.userBio.setText(Constant.OFFLINE);
                            }
                            holder.itemView.setOnClickListener(view -> {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra(Constant.VISIT_USER_ID, userIDs);
                                chatIntent.putExtra(Constant.VISIT_USER_NAME, name);
                                chatIntent.putExtra(Constant.VISIT_USER_IMAGE, image[0]);
                                startActivity(chatIntent);
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        chats.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName, userBio;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image_layUser);
            userName = itemView.findViewById(R.id.name_layUser);
            userBio = itemView.findViewById(R.id.bio_layUser);
        }
    }
}