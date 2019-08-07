package chat.infobox.hasnat.ume.ume.Profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import chat.infobox.hasnat.ume.ume.ProfileSetting.SettingsActivity;
import com.makarevich.dmitry.malina.chat.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    public String receiver_userID;
    public String senderID;
    private Button sendFriendRequest_Button, declineFriendRequest_Button;
    private TextView profileName;
    private TextView profileStatus;
    private TextView u_work;
    private ImageView profileImage, verified_icon;
    private DatabaseReference friendRequestReference;
    private String CURRENT_STATE;
    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference notificationDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        friendRequestReference.keepSynced(true);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        senderID = mAuth.getCurrentUser().getUid();
        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");
        friendsDatabaseReference.keepSynced(true);
        notificationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("notifications");
        notificationDatabaseReference.keepSynced(true);
        Toolbar mToolbar = findViewById(R.id.single_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("tag", "onClick : navigating back to back activity ");
                finish();
            }
        });
        receiver_userID = getIntent().getExtras().get("visitUserId").toString();
        sendFriendRequest_Button = findViewById(R.id.visitUserFrndRqstSendButton);
        declineFriendRequest_Button = findViewById(R.id.visitUserFrndRqstDeclineButton);
        profileName = findViewById(R.id.visitUserProfileName);
        profileStatus = findViewById(R.id.visitUserProfileStatus);
        verified_icon = findViewById(R.id.visit_verified_icon);
        profileImage = findViewById(R.id.visit_user_profile_image);
        u_work = findViewById(R.id.visit_work);
        TextView go_my_profile = findViewById(R.id.go_my_profile);
        verified_icon.setVisibility(View.INVISIBLE);
        CURRENT_STATE = "not_friends";
        userDatabaseReference.child(receiver_userID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String nickname = dataSnapshot.child("user_nickname").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String profession = dataSnapshot.child("user_profession").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                String verified = dataSnapshot.child("verified").getValue().toString();

                if (nickname.isEmpty()) {
                    profileName.setText(name);
                } else {
                    String full_name = name + " (" + nickname + ")";
                    profileName.setText(full_name);
                }

                if (profession.length() > 2) {
                    u_work.setText("  " + profession);
                }
                if (profession.equals("")) {
                    u_work.setText("  Пока не предоставлено");
                }
                profileStatus.setText(status);
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                if (verified.contains("true")) {
                    verified_icon.setVisibility(View.VISIBLE);
                }
                friendRequestReference.child(senderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(receiver_userID)) {
                                    String requestType = dataSnapshot.child(receiver_userID)
                                            .child("request_type").getValue().toString();
                                    if (requestType.equals("sent")) {
                                        CURRENT_STATE = "request_sent";
                                        sendFriendRequest_Button.setText("Отменить заявку в друзья");
                                        declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                        declineFriendRequest_Button.setEnabled(false);
                                    } else if (requestType.equals("received")) {
                                        CURRENT_STATE = "request_received";
                                        sendFriendRequest_Button.setText("Принять запрос на дружбу");
                                        declineFriendRequest_Button.setVisibility(View.VISIBLE);
                                        declineFriendRequest_Button.setEnabled(true);
                                        declineFriendRequest_Button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                declineFriendRequest();
                                            }
                                        });
                                    }
                                } else {
                                    friendsDatabaseReference.child(senderID)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        if (dataSnapshot.hasChild(receiver_userID)) {
                                                            CURRENT_STATE = "friends";
                                                            sendFriendRequest_Button.setText("Разблокировать этого человека");
                                                            declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                            declineFriendRequest_Button.setEnabled(false);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        declineFriendRequest_Button.setVisibility(View.GONE);
        declineFriendRequest_Button.setEnabled(false);
        if (!senderID.equals(receiver_userID)) {
            sendFriendRequest_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequest_Button.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        sendFriendRequest();
                    } else if (CURRENT_STATE.equals("request_sent")) {
                        cancelFriendRequest();
                    } else if (CURRENT_STATE.equals("request_received")) {
                        acceptFriendRequest();
                    } else if (CURRENT_STATE.equals("friends")) {
                        unfriendPerson();
                    }
                }
            });
        } else {
            sendFriendRequest_Button.setVisibility(View.INVISIBLE);
            declineFriendRequest_Button.setVisibility(View.INVISIBLE);
            go_my_profile.setVisibility(View.VISIBLE);
            go_my_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private void declineFriendRequest() {
        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequest_Button.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequest_Button.setText("Отправить запрос на дружбу");
                                                declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                declineFriendRequest_Button.setEnabled(false);
                                            }
                                        }

                                    });

                        }
                    }

                });
    }

    private void unfriendPerson() {
        friendsDatabaseReference.child(senderID).child(receiver_userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsDatabaseReference.child(receiver_userID).child(senderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequest_Button.setEnabled(true);
                                            CURRENT_STATE = "not_friends";
                                            sendFriendRequest_Button.setText("Отправить запрос на дружбу");
                                            declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                            declineFriendRequest_Button.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        Calendar myCalendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("EEEE, dd MMM, yyyy");
        final String friendshipDate = currentDate.format(myCalendar.getTime());
        friendsDatabaseReference.child(senderID).child(receiver_userID).child("date").setValue(friendshipDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        friendsDatabaseReference.child(receiver_userID).child(senderID).child("date").setValue(friendshipDate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                sendFriendRequest_Button.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendFriendRequest_Button.setText("Разблокировать этого человека");
                                                                                declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                                                declineFriendRequest_Button.setEnabled(false);
                                                                            }
                                                                        }

                                                                    });

                                                        }
                                                    }

                                                });

                                    }
                                });
                    }
                });
    }


    private void cancelFriendRequest() {
        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // after deleting data, just set button attributes
                                                sendFriendRequest_Button.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequest_Button.setText("Отправить запрос на дружбу");
                                                declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                declineFriendRequest_Button.setEnabled(false);
                                            }
                                        }

                                    });

                        }
                    }

                });

    }


    private void sendFriendRequest() {
        friendRequestReference.child(senderID).child(receiver_userID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiver_userID).child(senderID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> notificationData = new HashMap<>();
                                                notificationData.put("from", senderID);
                                                notificationData.put("type", "request");
                                                notificationDatabaseReference.child(receiver_userID).push().setValue(notificationData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    sendFriendRequest_Button.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    sendFriendRequest_Button.setText("Отменить заявку в друзья");
                                                                    declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                                    declineFriendRequest_Button.setEnabled(false);
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
}
