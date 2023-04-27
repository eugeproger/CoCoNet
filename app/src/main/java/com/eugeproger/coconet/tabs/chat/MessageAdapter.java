package com.eugeproger.coconet.tabs.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.simple.Message;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessagesViewHolder> {

    private List<Message> userMessages;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Message> userMessages) {
        this.userMessages = userMessages;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message, parent,false);

        auth = FirebaseAuth.getInstance();

        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        String messageSenderID = auth.getCurrentUser().getUid();
        Message message = userMessages.get(position);

        String fromUserID = message.getFrom();
        String fromMessageType = message.getType();

        usersRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.USERS).child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(Constant.IMAGE)) {
                    String receiverImage = snapshot.child(Constant.IMAGE).getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.avatar_profile).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (fromMessageType.equals(Constant.TEXT)) {
            holder.receiverMessageText.setVisibility(View.GONE);
            holder.receiverProfileImage.setVisibility(View.GONE);
            holder.senderMessageText.setVisibility(View.GONE);

            if (fromUserID.equals(messageSenderID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                //holder.senderMessageText.setBackgroundResource(R.drawable.design_messages_sender);
                holder.senderMessageText.setText(message.getMessage());
            } else {

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                //holder.receiverProfileImage.setVisibility(View.VISIBLE);

                //holder.receiverMessageText.setBackgroundResource(R.drawable.design_messages_receiver);
                holder.receiverMessageText.setText(message.getMessage());
            }
        }
        if (position == 0) {
            // Ustawienie marginesu dla pierwszego elementu
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 24, 0, 0);
            holder.itemView.setLayoutParams(params);
        } else {
            // Ustawienie zerowego marginesu dla pozostałych elementów
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            holder.itemView.setLayoutParams(params);
        }


    }

    @Override
    public int getItemCount() {
        return userMessages.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_layMessage);
            receiverMessageText = itemView.findViewById(R.id.receiver_layMessage);
            receiverProfileImage = itemView.findViewById(R.id.image_layMessage);
        }
    }
}
