package com.eugeproger.coconet.tabs.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.simple.Message;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_chatting, parent,false);
        auth = FirebaseAuth.getInstance();
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        String messageSenderID = auth.getCurrentUser().getUid();
        Message message = userMessages.get(position);
        String fromUserID = message.getFrom();
        String fromMessageType = message.getType();

        usersRef = ConfigurationFirebase.setRealtimeDatabaseRef()
                .child(NameFolderFirebase.USERS)
                .child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(Constant.IMAGE)) {
                    String receiverImage = snapshot.child(Constant.IMAGE).getValue().toString();
                    Picasso.get()
                            .load(receiverImage)
                            .placeholder(R.drawable.avatar_profile)
                            .into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.receivedFile.setVisibility(View.GONE);
        holder.sentFile.setVisibility(View.GONE);

        if (fromMessageType.equals(Constant.TEXT)) {
            if (fromUserID.equals(messageSenderID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setText(message.getMessage() +
                        "\n\n" + message.getTime() + " - " + message.getDate());
            } else {

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setText(message.getMessage() +
                        "\n\n" + message.getTime() + " - " + message.getDate());
            }
        } else if (fromMessageType.equals(Constant.IMAGE)) {
            if (fromUserID.equals(messageSenderID)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(holder.messageSenderPicture);
            } else {
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(holder.messageReceiverPicture);
            }
        } else if (fromMessageType.equals(Constant.FILE)) {
            if (fromUserID.equals(messageSenderID)) {
                holder.sentFile.setVisibility(View.VISIBLE);
            } else {
                holder.receivedFile.setVisibility(View.VISIBLE);
            }
        }
        if (fromUserID.equals(messageSenderID)) {
            holder.itemView.setOnClickListener(view -> {
                if (userMessages.get(position).getType().equals(Constant.FILE)) {
                    CharSequence options[] = new CharSequence[] {
                            "Download and view",
                            "Delete for me",
                            "Delete for everyone",
                            "Cancel",
                    };
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 1) {
                            deleteSentMessage(position, holder);
                        } else if (i == 0) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(userMessages.get(position).getMessage())
                            );
                            holder.itemView.getContext().startActivity(intent);
                        } else if (i == 2) {
                            deleteMessagesForEveryone(position, holder);
                        }
                    });
                    builder.show();
                }
                else if (userMessages.get(position).getType().equals(Constant.TEXT)) {
                    CharSequence options[] = new CharSequence[] {
                            "Delete for me",
                            "Delete for everyone",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView
                            .getContext()
                    );
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteSentMessage(position, holder);
                        } else if (i == 1) {
                            deleteMessagesForEveryone(position, holder);
                        }
                    });
                    builder.show();
                } else if (userMessages.get(position).getType().equals(Constant.IMAGE)) {
                    CharSequence options[] = new CharSequence[] {
                            "View",
                            "Delete for me",
                            "Delete for everyone",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView
                            .getContext()
                    );
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 1) {
                            deleteSentMessage(position, holder);
                        } else if (i == 0) {
                            Intent intent = new Intent(holder.itemView.getContext(),
                                    ImageViewerActivity.class
                            );
                            intent.putExtra(Constant.URL, userMessages.get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);
                        } else if (i == 2) {
                            deleteMessagesForEveryone(position, holder);
                        }
                    });
                    builder.show();
                }
            });
        } else {
            holder.itemView.setOnClickListener(view -> {
                if (userMessages.get(position).getType().equals(Constant.FILE)) {
                    CharSequence options[] = new CharSequence[] {
                            "Download and view",
                            "Delete for me",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 1) {
                            deleteReceivedMessages(position, holder);
                        } else if (i == 0) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(userMessages.get(position).getMessage())
                            );
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                    builder.show();
                }
                else if (userMessages.get(position).getType().equals(Constant.TEXT)) {
                    CharSequence options[] = new CharSequence[] {
                            "Delete for me",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView
                            .getContext()
                    );
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteReceivedMessages(position, holder);
                        }
                    });
                    builder.show();
                } else if (userMessages.get(position).getType().equals(Constant.IMAGE)) {
                    CharSequence options[] = new CharSequence[] {
                            "View",
                            "Delete for me",
                            "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView
                            .getContext());
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 1) {
                            deleteReceivedMessages(position, holder);
                        } else if (i == 0) {
                            Intent intent = new Intent(holder.itemView.getContext(),
                                    ImageViewerActivity.class
                            );
                            intent.putExtra(Constant.URL, userMessages.get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                    builder.show();
                }
            });
        }

        if (position == 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 24, 0, 0);
            holder.itemView.setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return userMessages.size();
    }

    private void deleteSentMessage(int position, MessagesViewHolder holder) {
        DatabaseReference rootRef = ConfigurationFirebase.setRealtimeDatabaseRef();
        rootRef.child(NameFolderFirebase.MESSAGES)
                .child(userMessages.get(position).getFrom())
                .child(userMessages.get(position).getTo())
                .child(userMessages.get(position).getMessageID())
                .removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utility.showShortToast(holder.itemView.getContext(),
                                "Deleted successfully"
                        );
                    } else {
                        Utility.showLengthToast(holder.itemView.getContext(), "Error occurred");
                    }
                });
    }

    private void deleteReceivedMessages(int position, MessagesViewHolder holder) {
        DatabaseReference rootRef = ConfigurationFirebase.setRealtimeDatabaseRef();
        rootRef.child(NameFolderFirebase.MESSAGES)
                .child(userMessages.get(position).getTo())
                .child(userMessages.get(position).getFrom())
                .child(userMessages.get(position).getMessageID())
                .removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utility.showShortToast(holder.itemView.getContext(),
                                "Deleted successfully"
                        );
                    } else {
                        Utility.showLengthToast(holder.itemView.getContext(), "Error occurred");
                    }
                });
    }

    private void deleteMessagesForEveryone(int position, MessagesViewHolder holder) {
        final DatabaseReference rootRef = ConfigurationFirebase.setRealtimeDatabaseRef();
        rootRef.child(NameFolderFirebase.MESSAGES)
                .child(userMessages.get(position).getTo())
                .child(userMessages.get(position).getFrom())
                .child(userMessages.get(position).getMessageID())
                .removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rootRef.child(NameFolderFirebase.MESSAGES)
                                .child(userMessages.get(position).getFrom())
                                .child(userMessages.get(position).getTo())
                                .child(userMessages.get(position).getMessageID())
                                .removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Utility.showShortToast(holder.itemView.getContext(),
                                                "Deleted successfully"
                                        );
                                    }
                                });
                    } else {
                        Utility.showLengthToast(holder.itemView.getContext(), "Error occurred");
                    }
                });
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public ImageView messageSenderPicture, messageReceiverPicture, sentFile, receivedFile;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_layMessage);
            receiverMessageText = itemView.findViewById(R.id.receiver_layMessage);
            receiverProfileImage = itemView.findViewById(R.id.image_layMessage);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view_layChatting);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view_layChatting);
            sentFile = itemView.findViewById(R.id.sender_file_layMessage);
            receivedFile = itemView.findViewById(R.id.receiver_file_layMessage);
        }
    }
}
