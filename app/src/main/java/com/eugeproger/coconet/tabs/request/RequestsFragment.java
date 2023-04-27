package com.eugeproger.coconet.tabs.request;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.simple.Contact;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.eugeproger.coconet.support.Utility;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private View requestsFragment;
    private RecyclerView mRequestList;
    private DatabaseReference chatRequestRef, usersRef, contactsRef;
    private FirebaseAuth auth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragment = inflater.inflate(R.layout.fragment_request, container, false);

        chatRequestRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.CHAT_REQUESTS);
        usersRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.USERS);
        contactsRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.CONTACTS);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();


        mRequestList = requestsFragment.findViewById(R.id.chat_request_RecyclerView_frag_request);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  requestsFragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(chatRequestRef.child(currentUserID), Contact.class).build();

        FirebaseRecyclerAdapter<Contact, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, RequestViewHolder>(options) {
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user,parent,false);
                RequestViewHolder holder = new RequestViewHolder(view);
                return holder;
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contact model) {

                holder.itemView.findViewById(R.id.buttons_box_user_box_layout).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.add_button_user_box_layout).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.cancel_button_user_box_layout).setVisibility(View.VISIBLE);

                final String listUserID = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child(Constant.REQUEST_TYPE).getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = snapshot.getValue().toString();

                            if (type.equals(Constant.RECEIVED)) {
                                usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.hasChild(Constant.IMAGE)) {

                                            String requestUserImage = snapshot.child(Constant.IMAGE).getValue().toString();


                                            Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                                        }
                                        String requestUserName = snapshot.child(Constant.NAME).getValue().toString();
                                        String requestUserBio = snapshot.child(Constant.BIO).getValue().toString();
                                        holder.userName.setText(requestUserName);
                                        holder.userBio.setText("wants to connect with you.");
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[] = new CharSequence[] {
                                                        "Accept",
                                                        "Reject"
                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                                                builder.setTitle(requestUserName + " chat request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (i == 0) {
                                                            contactsRef.child(currentUserID).child(listUserID).child(NameFolderFirebase.CONTACTS).setValue(Constant.SAVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        contactsRef.child(listUserID).child(currentUserID).child(NameFolderFirebase.CONTACTS).setValue(Constant.SAVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    chatRequestRef.child(currentUserID).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                chatRequestRef.child(listUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        Utility.showShortToast(getContext(), "Contact added!");
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if (i == 1) {
                                                            chatRequestRef.child(currentUserID).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        chatRequestRef.child(listUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Utility.showLengthToast(getContext(), "Contact deleted!");
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };

        mRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userBio;
        CircleImageView userProfileImage;
        ImageButton addButton, cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

        userName = itemView.findViewById(R.id.name_user_box_layout);
        userBio = itemView.findViewById(R.id.bio_user_box_layout);
        userProfileImage = itemView.findViewById(R.id.profile_image_user_box_layout);
        addButton = itemView.findViewById(R.id.add_button_user_box_layout);
        cancelButton = itemView.findViewById(R.id.cancel_button_user_box_layout);

        }
    }
}