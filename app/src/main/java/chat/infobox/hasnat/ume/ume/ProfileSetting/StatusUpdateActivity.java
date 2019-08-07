package chat.infobox.hasnat.ume.ume.ProfileSetting;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makarevich.dmitry.malina.chat.R;

import xyz.hasnat.sweettoast.SweetToast;

public class StatusUpdateActivity extends AppCompatActivity {

    private static final String TAG = "StatusUpdateActivity";

    private EditText status_from_input;
    private ProgressDialog progressDialog;
    private DatabaseReference statusDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        statusDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        status_from_input = findViewById(R.id.input_status);
        progressDialog = new ProgressDialog(this);
        Toolbar mToolbar = findViewById(R.id.update_status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Обновление статуса");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick : navigating back to 'SettingsActivity.class' ");
                finish();
            }
        });
        String previousStatus = getIntent().getExtras().get("ex_status").toString();
        status_from_input.setText(previousStatus);
        status_from_input.setSelection(status_from_input.getText().length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.update_status_done_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.status_update_done) {
            String new_status = status_from_input.getText().toString();
            changeProfileStatus(new_status);
        }
        return true;
    }

    private void changeProfileStatus(String new_status) {
        if (TextUtils.isEmpty(new_status)) {
            SweetToast.warning(getApplicationContext(), "Пожалуйста, напишите что-нибудь в статусе");
        } else {
            progressDialog.setMessage("Обновление статуса...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            statusDatabaseReference.child("user_status").setValue(new_status)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                finish();
                            } else {
                                SweetToast.warning(getApplicationContext(), "Произошла ошибка: не удалось обновить.");
                            }
                        }
                    });
        }
    }
}
