package com.eugeproger.coconet.login;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.eugeproger.coconet.AppMainActivity;
import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.ConfigurationFirebase;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.NameFolderFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.eugeproger.coconet.support.Utility;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private Button logInButton, phoneLogInButton;
    private EditText userLoginEmailField, userLoginPasswordField;
    private TextView signUpLink, createNewPasswordLink;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = ConfigurationFirebase.setRealtimeDatabaseRef().child(NameFolderFirebase.USERS);

        logInButton = findViewById(R.id.log_in_button);
        phoneLogInButton = findViewById(R.id.phone_log_in_button);
        userLoginEmailField = findViewById(R.id.user_login_email_field);
        userLoginPasswordField = findViewById(R.id.user_login_password_field);
        signUpLink = findViewById(R.id.sign_up_link);
        createNewPasswordLink = findViewById(R.id.create_new_password_link);
        progressDialog = new ProgressDialog(this);

        signUpLink.setOnClickListener(view -> sendUserToSignupActivity());
        logInButton.setOnClickListener(view -> allowUserToLogIn());
        phoneLogInButton.setOnClickListener(view -> {
            Intent phoneLoginIntent = new Intent(LoginActivity.this,
                    PhoneLoginActivity.class
            );
            startActivity(phoneLoginIntent);
        });
    }

    private void allowUserToLogIn() {
        String email = userLoginEmailField.getText().toString();
        String password = userLoginPasswordField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Utility.showShortToast(this,"Please, enter your email.");
        } else if (TextUtils.isEmpty(password)) {
            Utility.showShortToast(this,"Please, enter your password.");
        } else {
            progressDialog.setTitle("Sign in");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String currentUserID = firebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    usersRef.child(currentUserID)
                            .child(Constant.DEVICE_TOKEN)
                            .setValue(deviceToken)
                            .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            sendUserToAppMainActivity();
                            Utility.showShortToast(LoginActivity.this,
                                    "Logged in successful");
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    String message = task.getException().toString();
                    Utility.showLengthToast(LoginActivity.this, "Error: " + message);
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void sendUserToAppMainActivity() {
        Intent appMainIntent = new Intent(LoginActivity.this, AppMainActivity.class);
        appMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(appMainIntent);
        finish();
    }

    private void sendUserToSignupActivity() {
        Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(signupIntent);
    }
}
