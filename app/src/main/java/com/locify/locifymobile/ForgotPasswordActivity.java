package com.locify.locifymobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity implements ResetPasswordListener {
    private static final String TAG = "ForgotPassword";
    public static final String EMAIL_PROP = "email";

    EditText emailText;
    Button sendForgotPasswordButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailText = (EditText)findViewById(R.id.forgot_email);
        sendForgotPasswordButton = (Button)findViewById(R.id.btn_send_forgot);

        sendForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendForgottenPassword();
            }
        });
    }

    public void sendForgottenPassword() {
        Log.d(TAG, "Sending forgotten password...");

        if (!validate()) {
            return;
        }

        sendForgotPasswordButton.setEnabled(false);
        String email = emailText.getText().toString();
        LocifyClient client = LocifyClient.getInstance();
        client.sendResetInstructions(this, email, this);
    }

    @Override
    public void resetRequestSucceded(final String email) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                sendForgotPasswordButton.setEnabled(true);
                Intent data = new Intent();
                data.putExtra(EMAIL_PROP, email);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void resetRequestFailed(int statusCode) {
        sendForgotPasswordButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        return valid;
    }
}
