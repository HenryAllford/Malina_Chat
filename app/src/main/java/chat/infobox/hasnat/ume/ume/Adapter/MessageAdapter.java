package chat.infobox.hasnat.ume.ume.Adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import chat.infobox.hasnat.ume.ume.Model.Message;

import com.makarevich.dmitry.malina.chat.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private FirebaseAuth mAuth;
    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }
    @SuppressLint("RtlHardcoded")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String sender_UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Message message = messageList.get(position);
        String from_user_ID = message.getFrom();
        String from_message_TYPE = message.getType();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(from_user_ID);
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = Objects.requireNonNull(dataSnapshot.child("user_name").getValue()).toString();
                    String userProfileImage = Objects.requireNonNull(dataSnapshot.child("user_thumb_image").getValue()).toString();
                    Picasso.get()
                            .load(userProfileImage)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_profile_image)
                            .into(holder.user_profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (from_message_TYPE.equals("text")) {
            holder.receiver_text_message.setVisibility(View.INVISIBLE);
            holder.user_profile_image.setVisibility(View.INVISIBLE);
            holder.senderImageMsg.setVisibility(View.GONE);
            holder.receiverImageMsg.setVisibility(View.GONE);
            if (from_user_ID.equals(sender_UID)) {
                holder.sender_text_message.setBackgroundResource(R.drawable.single_message_text_another_background);
                holder.sender_text_message.setTextColor(Color.BLACK);
                holder.sender_text_message.setGravity(Gravity.LEFT);
                holder.sender_text_message.setText(message.getMessage());
            } else {
                holder.sender_text_message.setVisibility(View.INVISIBLE);
                holder.receiver_text_message.setVisibility(View.VISIBLE);
                holder.user_profile_image.setVisibility(View.VISIBLE);
                holder.receiver_text_message.setBackgroundResource(R.drawable.single_message_text_background);
                holder.receiver_text_message.setTextColor(Color.WHITE);
                holder.receiver_text_message.setGravity(Gravity.LEFT);
                holder.receiver_text_message.setText(message.getMessage());
            }
        }
        if (from_message_TYPE.equals("image")) {
            holder.sender_text_message.setVisibility(View.GONE);
            holder.receiver_text_message.setVisibility(View.GONE);
            if (from_user_ID.equals(sender_UID)) {
                holder.user_profile_image.setVisibility(View.GONE);
                holder.receiverImageMsg.setVisibility(View.GONE);
                Picasso.get()
                        .load(message.getMessage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.senderImageMsg);
                Log.e("tag", "from adapter, link : " + message.getMessage());
            } else {
                holder.user_profile_image.setVisibility(View.VISIBLE);
                holder.senderImageMsg.setVisibility(View.GONE);
                Picasso.get()
                        .load(message.getMessage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.receiverImageMsg);
                Log.e("tag", "from adapter, link : " + message.getMessage());

            }
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView sender_text_message, receiver_text_message;
        CircleImageView user_profile_image;
        RoundedImageView senderImageMsg, receiverImageMsg;

        MessageViewHolder(View view) {
            super(view);
            sender_text_message = view.findViewById(R.id.senderMessageText);
            receiver_text_message = view.findViewById(R.id.receiverMessageText);
            user_profile_image = view.findViewById(R.id.messageUserImage);
            senderImageMsg = view.findViewById(R.id.messageImageVsender);
            receiverImageMsg = view.findViewById(R.id.messageImageVreceiver);
        }
    }
}
