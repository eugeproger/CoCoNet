package com.eugeproger.coconet.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eugeproger.coconet.AppMainActivity;
import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendCode, verify;
    private EditText inputPhoneNumber, code;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String myVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private void initializeElements() {
        sendCode = findViewById(R.id.verification_code_button);
        verify = findViewById(R.id.verify_button);
        inputPhoneNumber = findViewById(R.id.phone_number_field);
        code = findViewById(R.id.verification_code_field);
        loadingBar = new ProgressDialog(this);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        initializeElements();
        initializeFirebase();

        sendCode.setOnClickListener(view -> sendVerificationCode());
        verify.setOnClickListener(view -> verifyVerificationCode());

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                String message = e.toString();
                Utility.showLengthToast(PhoneLoginActivity.this, "Error: " + message);
                //Utility.showLongToast(PhoneLoginActivity.this, "Enter correct phone number with country code.");

                sendCode.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verify.setVisibility(View.INVISIBLE);
                code.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                myVerificationId = verificationId;
                resendToken = token;

                loadingBar.dismiss();
                Utility.showLengthToast(PhoneLoginActivity.this, "Code has been send. Please, check your SMS-messages.");

                sendCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verify.setVisibility(View.VISIBLE);
                code.setVisibility(View.VISIBLE);
            }
        };
    }



    private void sendVerificationCode() {
        String phoneNumber = inputPhoneNumber.getText().toString();

        if (TextUtils.isEmpty(phoneNumber)) {
            Utility.showLengthToast(PhoneLoginActivity.this, "Enter your phone number first.");
        } else {
            loadingBar.setTitle("Phone verification");
            loadingBar.setMessage("Authenticating your phone number...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, PhoneLoginActivity.this, callbacks);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    Utility.showShortToast(PhoneLoginActivity.this, "Logging was successful!");
                    sendUserToAppMainActivity();
                } else {
                    String message = task.getException().toString();
                    Utility.showLengthToast(PhoneLoginActivity.this, message);
                }
            }
        });
    }

    private void verifyVerificationCode() {
        sendCode.setVisibility(View.INVISIBLE);
        inputPhoneNumber.setVisibility(View.INVISIBLE);

        String verificationCode = code.getText().toString();

        if (TextUtils.isEmpty(verificationCode)) {
            Utility.showLengthToast(PhoneLoginActivity.this, "Enter verification code.");
        } else {
            loadingBar.setTitle("Code verification");
            loadingBar.setMessage("Verification code is being checked...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(myVerificationId, verificationCode);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void sendUserToAppMainActivity() {
        Intent appMainIntent = new Intent(PhoneLoginActivity.this, AppMainActivity.class);
        startActivity(appMainIntent);
        finish();
    }
}