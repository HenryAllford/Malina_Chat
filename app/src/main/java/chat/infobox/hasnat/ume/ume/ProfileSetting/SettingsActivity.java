package chat.infobox.hasnat.ume.ume.ProfileSetting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makarevich.dmitry.malina.chat.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import xyz.hasnat.sweettoast.SweetToast;

public class SettingsActivity extends AppCompatActivity {

    private final static int GALLERY_PICK_CODE = 1;
    Bitmap thumb_Bitmap = null;
    private CircleImageView profile_settings_image;
    private TextView display_status, updatedMsg, recheckGender;
    private EditText display_name, display_email, user_phone, user_profession, user_nickname;
    private RadioButton maleRB, femaleRB;
    private DatabaseReference getUserDatabaseReference;
    private FirebaseAuth mAuth;
    private StorageReference mProfileImgStorageRef;
    private StorageReference thumb_image_ref;
    private ProgressDialog progressDialog;
    private String selectedGender = "", profile_download_url, profile_thumb_download_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        getUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        getUserDatabaseReference.keepSynced(true);
        mProfileImgStorageRef = FirebaseStorage.getInstance().getReference().child("profile_image");
        thumb_image_ref = FirebaseStorage.getInstance().getReference().child("thumb_image");
        profile_settings_image = findViewById(R.id.profile_img);
        display_name = findViewById(R.id.user_display_name);
        user_nickname = findViewById(R.id.user_nickname);
        user_profession = findViewById(R.id.profession);
        display_email = findViewById(R.id.userEmail);
        user_phone = findViewById(R.id.phone);
        display_status = findViewById(R.id.userProfileStatus);
        ImageView editPhotoIcon = findViewById(R.id.editPhotoIcon);
        Button saveInfoBtn = findViewById(R.id.saveInfoBtn);
        ImageView editStatusBtn = findViewById(R.id.statusEdit);
        updatedMsg = findViewById(R.id.updatedMsg);
        recheckGender = findViewById(R.id.recheckGender);
        recheckGender.setVisibility(View.VISIBLE);
        maleRB = findViewById(R.id.maleRB);
        femaleRB = findViewById(R.id.femaleRB);
        Toolbar toolbar = findViewById(R.id.profile_settings_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Профиль");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressDialog = new ProgressDialog(this);
        getUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String nickname = dataSnapshot.child("user_nickname").getValue().toString();
                String profession = dataSnapshot.child("user_profession").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String email = dataSnapshot.child("user_email").getValue().toString();
                String phone = dataSnapshot.child("user_mobile").getValue().toString();
                String gender = dataSnapshot.child("user_gender").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                display_status.setText(status);
                display_name.setText(name);
                display_name.setSelection(display_name.getText().length());
                user_nickname.setText(nickname);
                user_nickname.setSelection(user_nickname.getText().length());
                user_profession.setText(profession);
                user_profession.setSelection(user_profession.getText().length());
                user_phone.setText(phone);
                user_phone.setSelection(user_phone.getText().length());
                display_email.setText(email);
                if (!image.equals("default_image")) {
                    Picasso.get()
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .into(profile_settings_image);
                }

                if (gender.equals("Male")) {
                    maleRB.setChecked(true);
                } else if (gender.equals("Female")) {
                    femaleRB.setChecked(true);
                } else {
                    return;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        editPhotoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, GALLERY_PICK_CODE);
            }
        });
        saveInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = display_name.getText().toString();
                String uNickname = user_nickname.getText().toString();
                String uPhone = user_phone.getText().toString();
                String uProfession = user_profession.getText().toString();

                saveInformation(uName, uNickname, uPhone, uProfession, selectedGender);
            }
        });
        editStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String previous_status = display_status.getText().toString();
                Intent statusUpdateIntent = new Intent(SettingsActivity.this, StatusUpdateActivity.class);
                statusUpdateIntent.putExtra("ex_status", previous_status);
                startActivity(statusUpdateIntent);
            }
        });
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }
    public void selectedGenderRB(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.maleRB:
                if (checked) {
                    selectedGender = "Male";
                    recheckGender.setVisibility(View.GONE);
                    break;
                }
            case R.id.femaleRB:
                if (checked) {
                    selectedGender = "Female";
                    recheckGender.setVisibility(View.GONE);
                    break;
                }
        }
    }


    private void saveInformation(String uName, String uNickname, String uPhone, String uProfession, String uGender) {
        if (uGender.length() < 1) {
            recheckGender.setTextColor(Color.RED);
        } else if (TextUtils.isEmpty(uName)) {
            SweetToast.error(this, "Упс! Твое имя не может быть пустым");
        } else if (uName.length() < 3 || uName.length() > 40) {
            SweetToast.warning(this, "Ваше имя должно быть от 3 до 40 символов");
        } else if (TextUtils.isEmpty(uPhone)) {
            SweetToast.error(this, "Ваш номер мобильного телефона требуется.");
        } else if (uPhone.length() < 11) {
            SweetToast.warning(this, "Сожалею! Ваш мобильный номер слишком короткий");
        } else {
            getUserDatabaseReference.child("user_name").setValue(uName);
            getUserDatabaseReference.child("user_nickname").setValue(uNickname);
            getUserDatabaseReference.child("search_name").setValue(uName.toLowerCase());
            getUserDatabaseReference.child("user_profession").setValue(uProfession);
            getUserDatabaseReference.child("user_mobile").setValue(uPhone);
            getUserDatabaseReference.child("user_gender").setValue(uGender)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            updatedMsg.setVisibility(View.VISIBLE);
                            new Timer().schedule(new TimerTask() {
                                public void run() {
                                    SettingsActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            updatedMsg.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }, 1500);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.show();
                final Uri resultUri = result.getUri();
                File thumb_filePath_Uri = new File(resultUri.getPath());
                final String user_id = mAuth.getCurrentUser().getUid();
                try {
                    thumb_Bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(45)
                            .compressToBitmap(thumb_filePath_Uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final StorageReference filePath = mProfileImgStorageRef.child(user_id + ".jpg");

                UploadTask uploadTask = filePath.putFile(resultUri);
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (!task.isSuccessful()) {
                            SweetToast.error(SettingsActivity.this, "Ошибка фотографии профиля: " + task.getException().getMessage());
                        }
                        profile_download_url = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            profile_download_url = task.getResult().toString();
                            Log.e("tag", "profile url: " + profile_download_url);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            thumb_Bitmap.compress(Bitmap.CompressFormat.JPEG, 45, outputStream);
                            final byte[] thumb_byte = outputStream.toByteArray();
                            final StorageReference thumb_filePath = thumb_image_ref.child(user_id + "jpg");
                            UploadTask thumb_uploadTask = thumb_filePath.putBytes(thumb_byte);
                            Task<Uri> thumbUriTask = thumb_uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        SweetToast.error(SettingsActivity.this, "Ошибка изображения: " + task.getException().getMessage());
                                    }
                                    profile_thumb_download_url = thumb_filePath.getDownloadUrl().toString();
                                    return thumb_filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    profile_thumb_download_url = task.getResult().toString();
                                    Log.e("tag", "thumb url: " + profile_thumb_download_url);
                                    if (task.isSuccessful()) {
                                        Log.e("tag", "thumb profile updated");
                                        HashMap<String, Object> update_user_data = new HashMap<>();
                                        update_user_data.put("user_image", profile_download_url);
                                        update_user_data.put("user_thumb_image", profile_thumb_download_url);
                                        getUserDatabaseReference.updateChildren(new HashMap<>(update_user_data))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.e("tag", "thumb profile updated");
                                                        progressDialog.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("tag", "for thumb profile: " + e.getMessage());
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                SweetToast.info(SettingsActivity.this, "Не удалось обрезать изображение.");
            }
        }

    }


}
