package com.locify.locifymobile;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity implements SignupListener {
    private static final String TAG = "SignupActivity";
    private static final int MIN_PASSWORD_LENGHT = 5;
    public static final String NAME_PROP = "name";
    public static final String EMAIL_PROP = "email";

    EditText nameText;
    EditText emailText;
    EditText passwordField;
    EditText confirmPasswordField;
    Button signupButton;
    TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        nameText = (EditText)findViewById(R.id.signup_name);
        emailText = (EditText)findViewById(R.id.signup_email);
        passwordField = (EditText)findViewById(R.id.signup_password);
        confirmPasswordField = (EditText)findViewById(R.id.signup_password_confirm);
        signupButton = (Button)findViewById(R.id.btn_signup);
        loginLink = (TextView)findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            return;
        }

        signupButton.setEnabled(false);

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordField.getText().toString();
        LocifyClient client = LocifyClient.getInstance();
        client.signUp(this, name, email, password, this);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (name.isEmpty()) {
            nameText.setError(getResources().getString(R.string.empty_name));
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty()) {
            passwordField.setError(getResources().getString(R.string.empty_password));
            valid = false;
        } else if(password.length() < MIN_PASSWORD_LENGHT) {
            passwordField.setError(getResources().getString(R.string.password_length_error, MIN_PASSWORD_LENGHT));
            valid = false;
        } else {
            passwordField.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordField.setError(getResources().getString(R.string.empty_confirm_password));
            valid = false;
        } else {
            confirmPasswordField.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError(getResources().getString(R.string.passwords_dont_match));
            valid = false;
        } else {
            confirmPasswordField.setError(null);
        }

        return valid;
    }

    @Override
    public void signupSucceded(final String name, final String email, String password) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                signupButton.setEnabled(true);
                Intent data = new Intent();
                data.putExtra(NAME_PROP, name);
                data.putExtra(EMAIL_PROP, email);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void signupFailed(int statusCode) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), R.string.SignupFailedMsg, Toast.LENGTH_LONG).show();
                signupButton.setEnabled(true);
            }
        });
    }
}