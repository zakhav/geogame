package com.locify.locifymobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.PersistentCookieStore;

import cz.msebera.android.httpclient.client.CookieStore;

public class LoginActivity extends AppCompatActivity implements LoginListener {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_FORGOT_PASSWORD = 1;

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;
    TextView forgotPasswordLink;
    View authProgressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = (EditText)findViewById(R.id.input_email);
        passwordText = (EditText)findViewById(R.id.input_password);
        loginButton = (Button)findViewById(R.id.btn_login);
        signupLink = (TextView)findViewById(R.id.link_signup);
        forgotPasswordLink = (TextView)findViewById(R.id.link_forgot_password);
        authProgressView = findViewById(R.id.auth_progress);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivityForResult(intent, REQUEST_FORGOT_PASSWORD);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Logging in...");

        if (!validate()) {
            return;
        }

        loginButton.setEnabled(false);
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        addLoginProgress();

        LocifyClient client = LocifyClient.getInstance();
        client.login(this, this, email, password);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Resources res = getResources();
                Toast.makeText(getBaseContext(),
                        res.getString(R.string.signup_account_created,
                                data.getStringExtra(SignupActivity.NAME_PROP)),
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_FORGOT_PASSWORD) {
            if (resultCode == RESULT_OK) {
                Resources res = getResources();
                Toast.makeText(getBaseContext(),
                        res.getString(R.string.reset_password_instructions,
                                data.getStringExtra(ForgotPasswordActivity.EMAIL_PROP)),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty()) {
            passwordText.setError(getResources().getString(R.string.empty_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    private void addLoginProgress() {
        authProgressView.setVisibility(View.VISIBLE);
    }

    private void removeLoginProgress() {
        authProgressView.setVisibility(View.GONE);
    }

    @Override
    public void loginSucceded() {
        Log.i(TAG, "Login succeded!!!");
        loginButton.setEnabled(true);
        finish();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void loginFailed(int statusCode) {
        Log.e(TAG, "Login failed: " + statusCode);
        removeLoginProgress();
        Toast.makeText(getBaseContext(), R.string.LoginFailedMsg, Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }
}
