package chat.infobox.hasnat.ume.ume.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import chat.infobox.hasnat.ume.ume.Model.Requests;
import chat.infobox.hasnat.ume.ume.Profile.ProfileActivity;
import com.makarevich.dmitry.malina.chat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    String user_UId;
    private View view;
    private RecyclerView request_list;
    private Context context;
    private DatabaseReference databaseReference;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference friendReqDatabaseReference;


    public RequestsFragment() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_requests, container, false);

        request_list = view.findViewById(R.id.requestList);
        request_list.setHasFixedSize(true);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user_UId = mAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(user_UId);

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");
        friendReqDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //linearLayoutManager.setStackFromEnd(true);
        request_list.setHasFixedSize(true);
        request_list.setLayoutManager(linearLayoutManager);


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requests> recyclerOptions = new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(databaseReference, Requests.class)
                .build();

        FirebaseRecyclerAdapter<Requests, RequestsVH> adapter = new FirebaseRecyclerAdapter<Requests, RequestsVH>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsVH holder, int position, @NonNull Requests model) {
                final String userID = getRef(position).getKey();
                DatabaseReference getTypeReference = getRef(position).child("request_type").getRef();
                getTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String requestType = dataSnapshot.getValue().toString();
                            holder.verified_icon.setVisibility(View.GONE);

                            if (requestType.equals("received")) {
                                holder.re_icon.setVisibility(View.VISIBLE);
                                holder.se_icon.setVisibility(View.GONE);
                                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String userVerified = dataSnapshot.child("verified").getValue().toString();
                                        final String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String user_status = dataSnapshot.child("user_status").getValue().toString();

                                        holder.name.setText(userName);
                                        holder.status.setText(user_status);

                                        if (!userThumbPhoto.equals("default_image")) {
                                            Picasso.get()
                                                    .load(userThumbPhoto)
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(holder.user_photo, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            Picasso.get()
                                                                    .load(userThumbPhoto)
                                                                    .placeholder(R.drawable.default_profile_image)
                                                                    .into(holder.user_photo);
                                                        }
                                                    });
                                        }

                                        if (userVerified.contains("true")) {
                                            holder.verified_icon.setVisibility(View.VISIBLE);
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence[] options = new CharSequence[]{"Подтвердить Запрос", "Завершить Запрос", userName + " профиль"};

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if (which == 0) {
                                                            Calendar myCalendar = Calendar.getInstance();
                                                            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("EEEE, dd MMM, yyyy");
                                                            final String friendshipDate = currentDate.format(myCalendar.getTime());

                                                            friendsDatabaseReference.child(user_UId).child(userID).child("date").setValue(friendshipDate)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            friendsDatabaseReference.child(userID).child(user_UId).child("date").setValue(friendshipDate)
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            /*
                                                                                             *  because of accepting friend request,
                                                                                             *  there have no more request them. So, for delete these node
                                                                                             */
                                                                                            friendReqDatabaseReference.child(user_UId).child(userID).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                // delete from users friend_requests node, receiver >> sender > values
                                                                                                                friendReqDatabaseReference.child(userID).child(user_UId).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    // after deleting data
                                                                                                                                    Snackbar snackbar = Snackbar
                                                                                                                                            .make(view, "Теперь вы друзья", Snackbar.LENGTH_LONG);
                                                                                                                                    // Changing message text color
                                                                                                                                    View sView = snackbar.getView();
                                                                                                                                    sView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                                                                                                                                    TextView textView = sView.findViewById(android.support.design.R.id.snackbar_text);
                                                                                                                                    textView.setTextColor(Color.WHITE);
                                                                                                                                    snackbar.show();
                                                                                                                                }
                                                                                                                            }

                                                                                                                        });

                                                                                                            }
                                                                                                        }

                                                                                                    }); //

                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }


                                                        if (which == 1) {
                                                            friendReqDatabaseReference.child(user_UId).child(userID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                friendReqDatabaseReference.child(userID).child(user_UId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Snackbar snackbar = Snackbar
                                                                                                            .make(view, "Запрос Завершен", Snackbar.LENGTH_LONG);
                                                                                                    View sView = snackbar.getView();
                                                                                                    sView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                                                                                                    TextView textView = sView.findViewById(android.support.design.R.id.snackbar_text);
                                                                                                    textView.setTextColor(Color.WHITE);
                                                                                                    snackbar.show();

                                                                                                }
                                                                                            }

                                                                                        });

                                                                            }
                                                                        }

                                                                    });
                                                        }
                                                        if (which == 2) {
                                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                            profileIntent.putExtra("visitUserId", userID);
                                                            startActivity(profileIntent);
                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            if (requestType.equals("sent")) {
                                holder.re_icon.setVisibility(View.GONE);
                                holder.se_icon.setVisibility(View.VISIBLE);
                                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String userVerified = dataSnapshot.child("verified").getValue().toString();
                                        final String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String user_status = dataSnapshot.child("user_status").getValue().toString();

                                        holder.name.setText(userName);
                                        holder.status.setText(user_status);

                                        if (!userThumbPhoto.equals("default_image")) {
                                            Picasso.get()
                                                    .load(userThumbPhoto)
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(holder.user_photo, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            Picasso.get()
                                                                    .load(userThumbPhoto)
                                                                    .placeholder(R.drawable.default_profile_image)
                                                                    .into(holder.user_photo);
                                                        }
                                                    });
                                        }

                                        if (userVerified.contains("true")) {
                                            holder.verified_icon.setVisibility(View.VISIBLE);
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence[] options = new CharSequence[]{"Отправка запроса завершена", userName + " профиль"};
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {
                                                            friendReqDatabaseReference.child(user_UId).child(userID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                friendReqDatabaseReference.child(userID).child(user_UId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Snackbar snackbar = Snackbar
                                                                                                            .make(view, "Отправка запроса завершена", Snackbar.LENGTH_LONG);
                                                                                                    View sView = snackbar.getView();
                                                                                                    sView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                                                                                                    TextView textView = sView.findViewById(android.support.design.R.id.snackbar_text);
                                                                                                    textView.setTextColor(Color.WHITE);
                                                                                                    snackbar.show();
                                                                                                }
                                                                                            }

                                                                                        });

                                                                            }
                                                                        }

                                                                    });
                                                        }
                                                        if (which == 1) {
                                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                            profileIntent.putExtra("visitUserId", userID);
                                                            startActivity(profileIntent);
                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }

                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_single, viewGroup, false);
                return new RequestsVH(view);
            }
        };
        request_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsVH extends RecyclerView.ViewHolder {
        TextView name, status;
        CircleImageView user_photo;
        ImageView re_icon, se_icon, verified_icon;

        RequestsVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.r_profileName);
            user_photo = itemView.findViewById(R.id.r_profileImage);
            status = itemView.findViewById(R.id.r_profileStatus);
            re_icon = itemView.findViewById(R.id.receivedIcon);
            se_icon = itemView.findViewById(R.id.sentIcon);
            verified_icon = itemView.findViewById(R.id.verifiedIcon);
        }
    }

}
