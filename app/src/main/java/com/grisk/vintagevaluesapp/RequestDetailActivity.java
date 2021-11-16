package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String DOCID = "docID";
    private String docID;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private Request request;
    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy", Locale.US);

    private TextView name;
    private TextView bags;
    private TextView description;
    private TextView timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);


        name = findViewById(R.id.name_label_items);
        bags = findViewById(R.id.bags_label_items);
        description = findViewById(R.id.location_description_label_items);
        timeStamp = findViewById(R.id.time_stamp_label_item);


        Intent intent = getIntent();
        docID = intent.getStringExtra(DOCID);

        DocumentReference docRef = mDb.collection(RequestPickup.REQUESTS).document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                request = documentSnapshot.toObject(Request.class);
                Log.d("Test", request.getFirst());

                String fullName = request.getFirst() + " " + request.getLast();

                name.setText(fullName);
                bags.setText(request.getBags());
                description.setText(request.getPickupDescription());
                timeStamp.setText(format.format(request.getCreatedTime()));

            }
        });
    }
}