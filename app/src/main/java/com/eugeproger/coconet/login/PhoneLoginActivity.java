package com.eugeproger.coconet.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eugeproger.coconet.R;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendCode, verify;
    private EditText phoneNumber, code;

    private void initializeElements() {
        sendCode = findViewById(R.id.verification_code_button);
        verify = findViewById(R.id.verify_button);
        phoneNumber = findViewById(R.id.phone_number_field);
        code = findViewById(R.id.verification_code_field);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        initializeElements();

        sendCode.setOnClickListener(view -> {
            sendCode.setVisibility(View.INVISIBLE);
            phoneNumber.setVisibility(View.INVISIBLE);

            verify.setVisibility(View.VISIBLE);
            code.setVisibility(View.VISIBLE);
        });
    }


}