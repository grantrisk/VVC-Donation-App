package com.grisk.vintagevaluesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

public class PopupMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_menu);

        // Make activity size a specific percent smaller than the phone screen
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // Int values are the percents were making the new activity
        getWindow().setLayout((int) (width * .9), (int) (height * .2));

    }

    public void onFinishedPickup (View view) {

        // Update requestCompleted in database

        // Close popup
        finish();
    }
}