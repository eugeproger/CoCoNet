package com.eugeproger.coconet.tabs.contact;

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
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.ConfigurationFirebase;
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

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView mContactsList;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth auth;
    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactsList = (RecyclerView) contactsView.findViewById(R.id.contacts_list_frag_contacts);
        mContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        contactsRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.CONTACTS).child(currentUserID);
        usersRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.USERS);


        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(contactsRef, Contact.class).build();

        final FirebaseRecyclerAdapter<Contact, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user, parent,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contact model) {
                String userIDs = getRef(position).getKey();
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(Constant.IMAGE)) {
                            String profileImage = snapshot.child(Constant.IMAGE).getValue().toString();
                            String profileBio = snapshot.child(Constant.BIO).getValue().toString();
                            String profileName = snapshot.child(Constant.NAME).getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userBio.setText(profileBio);
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                        }
                        else {
                            String profileBio = snapshot.child(Constant.BIO).getValue().toString();
                            String profileName = snapshot.child(Constant.NAME).getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userBio.setText(profileBio);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };

        mContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userBio;
        CircleImageView userImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.name_user_box_layout);
            userBio = itemView.findViewById(R.id.bio_user_box_layout);
            userImage = itemView.findViewById(R.id.profile_image_user_box_layout);

        }
    }
}