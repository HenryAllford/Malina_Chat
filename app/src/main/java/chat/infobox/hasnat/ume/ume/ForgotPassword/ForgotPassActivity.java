package chat.infobox.hasnat.ume.ume.ForgotPassword;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import chat.infobox.hasnat.ume.ume.LoginReg.LoginActivity;
import com.makarevich.dmitry.malina.chat.R;

import java.util.Timer;
import java.util.TimerTask;

import xyz.hasnat.sweettoast.SweetToast;


public class ForgotPassActivity extends AppCompatActivity {
    private EditText forgotEmail;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        //Toolbar mToolbar = findViewById(R.id.fp_toolbar);
       // setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("Восстановление доступа");
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        auth = FirebaseAuth.getInstance();
        forgotEmail = findViewById(R.id.forgotEmail);
        Button resetPassButton = findViewById(R.id.resetPassButton);
        resetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = forgotEmail.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    SweetToast.error(ForgotPassActivity.this, "Требуется электронная почта");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    SweetToast.error(ForgotPassActivity.this, "Формат введенной вами почты является запрещенным");
                } else {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                emailSentSuccessPopUp();
                                new Timer().schedule(new TimerTask() {
                                    public void run() {
                                        ForgotPassActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                auth.signOut();
                                                Intent mainIntent = new Intent(ForgotPassActivity.this, LoginActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                                SweetToast.info(ForgotPassActivity.this, "Пожалуйста проверьте ваш e-mail");
                                            }
                                        });
                                    }
                                }, 8000);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            SweetToast.error(ForgotPassActivity.this, "Упс!! " + e.getMessage());
                        }
                    });
                }
            }
        });

    }

    private void emailSentSuccessPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassActivity.this);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(ForgotPassActivity.this).inflate(R.layout.register_success_popup, null);
        TextView successMessage = view.findViewById(R.id.successMessage);
        successMessage.setText("Ссылка для восстановления пароля отправлена вам на почтовый адрес\nПожалуйста проверьте входящие письма. Спасибо");
        builder.setCancelable(true);
        builder.setView(view);
        builder.show();
    }
}
