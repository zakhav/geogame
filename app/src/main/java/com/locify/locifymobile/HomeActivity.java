package com.locify.locifymobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void geoCachingMenuSelected(View v) {
        Intent intent = new Intent(getApplicationContext(), GeoCachingActivity.class);
        startActivity(intent);
    }

    public void localSearchMenuSelected(View v) {
    }

    public void waypointsMenuSelected(View v) {
    }

    public void exitMenuSelected(View v) {
        LocifyClient client = LocifyClient.getInstance();
        client.logout(this);
        finish();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void settingsMenuSelected(View v) {
    }
}
