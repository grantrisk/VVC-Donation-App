package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PopupMenu extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    public static final String DOCID = "docID";
    private String docID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_menu);

        Intent intent = getIntent();
        docID = intent.getStringExtra(DOCID);

        // Make activity size a specific percent smaller than the phone screen
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // Int values are the percents were making the new activity
        getWindow().setLayout((int) (width * .9), (int) (height * .2));

    }

    public void onFinishedPickup(View view) {

        // Update requestCompleted in database
        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        String currentUser = mAuth.getCurrentUser().getUid();

        // Set requestAccepted in DB to true
        mDb.collection(FulfillPickup.REQUESTS).document(docID).update(
                "requestCompleted", true
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Successfully Completed Pickup Request", Toast.LENGTH_SHORT).show();

                // Close popup
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed Completing Pickup Request. Retry Later.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}