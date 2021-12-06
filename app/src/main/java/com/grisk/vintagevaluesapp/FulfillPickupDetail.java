package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FulfillPickupDetail extends AppCompatActivity {

    public static final String DOCID = "docID";
    private String docID;

    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Request request;
    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy", Locale.US);

    private TextView firstName;
    private TextView lastName;
    private TextView bags;
    private TextView description;
    private TextView timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfill_pickup_detail);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        firstName = findViewById(R.id.name_label_items);
        lastName = findViewById(R.id.last_name_label_items);
        bags = findViewById(R.id.bags_label_items);
        description = findViewById(R.id.location_description_label_items);
        timeStamp = findViewById(R.id.time_stamp_label_item);

        Intent intent = getIntent();
        docID = intent.getStringExtra(DOCID);

        DocumentReference docRef = mDb.collection(FulfillPickup.REQUESTS).document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                request = documentSnapshot.toObject(Request.class);

                firstName.setText(request.getFirst());
                lastName.setText(request.getLast());
                bags.setText(request.getBags());
                description.setText(request.getPickupDescription());
                timeStamp.setText(format.format(request.getCreatedTime()));


            }
        });
    }

    public void onYesButton(View view) {

        String currentUser = mAuth.getCurrentUser().getUid();

        // Set requestAccepted in DB to true
        mDb.collection(FulfillPickup.REQUESTS).document(docID).update(
                "requestAccepted", true,
                "pickupUid", currentUser
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Successfully Accepted Pickup Request", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed Accepting Pickup Request. Retry Later.", Toast.LENGTH_SHORT).show();
            }
        });

        // Return to previous activity
        finish();
    }
}